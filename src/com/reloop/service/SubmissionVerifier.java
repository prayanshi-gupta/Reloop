package com.reloop.service;

import com.reloop.dao.*;
import com.reloop.exception.DatabaseException;
import com.reloop.exception.ValidationException;
import com.reloop.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles operator verification of recycling submissions.
 * Executes point calculations and ACID wallet updates using JDBC transactions.
 * Demonstrates: Transaction management (setAutoCommit, commit, rollback), Business validation.
 */
public class SubmissionVerifier {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final SubmissionItemDAO itemDAO = new SubmissionItemDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final PointsCalculator calculator = new PointsCalculator();
    private final WeightValidator validator = new WeightValidator();

    /**
     * Operator confirms and verifies a submission with final measured quantities/weights.
     * Atomically:
     * 1. Updates submission items with verified quantities and calculated points.
     * 2. Sets submission status to VERIFIED with operator ID.
     * 3. Credits the customer's wallet.
     * 4. Logs a points transaction audit record.
     */
    public int verifySubmission(int submissionId, int operatorId, List<SubmissionItem> verifiedItems)
            throws ValidationException, DatabaseException {

        Submission submission = submissionDAO.findById(submissionId);
        if (submission == null) {
            throw new ValidationException("Submission not found: " + submissionId);
        }
        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new ValidationException("Submission has already been processed (Current status: " + submission.getStatus() + ")");
        }
        if (verifiedItems == null || verifiedItems.isEmpty()) {
            throw new ValidationException("At least one verified material item is required");
        }

        // Validate items and calculate points
        int totalPoints = 0;
        for (SubmissionItem item : verifiedItems) {
            Material mat = materialDAO.findById(item.getMaterialId());
            if (mat == null) {
                throw new ValidationException("Material ID " + item.getMaterialId() + " not found");
            }
            validator.validate(mat, item.getQuantity());
            int pts = calculator.calculatePoints(mat, item.getQuantity());
            item.setPointsEarned(pts);
            item.setSubmissionId(submissionId);
            totalPoints += pts;
        }

        // Execute ACID transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Delete draft items and insert verified line items
                itemDAO.deleteBySubmissionId(submissionId, con);
                for (SubmissionItem item : verifiedItems) {
                    itemDAO.create(item, con);
                }

                // 2. Mark submission as VERIFIED
                submissionDAO.updateStatus(submissionId, SubmissionStatus.VERIFIED, operatorId, null, con);

                // 3. Credit customer wallet
                walletDAO.creditPoints(submission.getCustomerId(), totalPoints, con);

                // 4. Retrieve wallet ID to write transaction record
                Wallet wallet = walletDAO.findByCustomerId(submission.getCustomerId());
                if (wallet != null) {
                    PointsTransaction tx = new PointsTransaction(
                        0,
                        wallet.getId(),
                        submissionId,
                        totalPoints,
                        TransactionType.EARNED,
                        null
                    );
                    transactionDAO.create(tx, con);
                }

                con.commit();
                return totalPoints;
            } catch (SQLException e) {
                con.rollback();
                throw new DatabaseException("Transaction failed while verifying submission: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error: " + e.getMessage(), e);
        }
    }

    /**
     * Operator rejects a submission with a mandatory reason (e.g. contaminated waste, unaccepted materials).
     */
    public void rejectSubmission(int submissionId, int operatorId, String reason)
            throws ValidationException, DatabaseException {

        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("A rejection reason must be provided");
        }

        Submission submission = submissionDAO.findById(submissionId);
        if (submission == null) {
            throw new ValidationException("Submission not found: " + submissionId);
        }
        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new ValidationException("Submission cannot be rejected because it is already " + submission.getStatus());
        }

        try (Connection con = DBConnection.getConnection()) {
            submissionDAO.updateStatus(submissionId, SubmissionStatus.REJECTED, operatorId, reason.trim(), con);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to reject submission: " + e.getMessage(), e);
        }
    }
}
