package com.reloop.service;

import com.reloop.dao.*;
import com.reloop.exception.DatabaseException;
import com.reloop.exception.ValidationException;
import com.reloop.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Coordinates recycling submission pre-registration and retrieval.
 */
public class RecyclingService {

    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final SubmissionItemDAO itemDAO = new SubmissionItemDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final StationDAO stationDAO = new StationDAO();

    /**
     * Customer creates an initial drop-off submission (Status: PENDING).
     */
    public Submission createSubmission(int customerId, int stationId, List<SubmissionItem> draftItems)
            throws ValidationException, DatabaseException {

        RecyclingStation station = stationDAO.findById(stationId);
        if (station == null) {
            throw new ValidationException("Selected recycling station does not exist");
        }

        if (draftItems == null || draftItems.isEmpty()) {
            throw new ValidationException("Submission must contain at least one recyclable item");
        }

        Submission submission = new Submission();
        submission.setCustomerId(customerId);
        submission.setStationId(stationId);
        submission.setStatus(SubmissionStatus.PENDING);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int subId = submissionDAO.create(submission, con);
                submission.setId(subId);

                for (SubmissionItem item : draftItems) {
                    Material mat = materialDAO.findById(item.getMaterialId());
                    if (mat == null) {
                        throw new ValidationException("Unknown material ID: " + item.getMaterialId());
                    }
                    if (item.getQuantity() <= 0) {
                        throw new ValidationException("Quantity must be positive for " + mat.getName());
                    }
                    item.setSubmissionId(subId);
                    item.setMaterialName(mat.getName());
                    item.setUnit(mat.getUnit());
                    // In draft stage, points can be estimated
                    item.setPointsEarned((int) Math.round(item.getQuantity() * mat.getPointsPerUnit()));
                    itemDAO.create(item, con);
                }

                con.commit();
                return submission;
            } catch (SQLException e) {
                con.rollback();
                throw new DatabaseException("Failed to save submission: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error: " + e.getMessage(), e);
        }
    }

    public List<Submission> getCustomerSubmissions(int customerId) throws DatabaseException {
        List<Submission> submissions = submissionDAO.findByCustomerId(customerId);
        for (Submission s : submissions) {
            List<SubmissionItem> items = itemDAO.findBySubmissionId(s.getId());
            s.setItems(items);
            s.calculateTotalPoints();
        }
        return submissions;
    }

    public List<Submission> getPendingSubmissionsForStation(int stationId) throws DatabaseException {
        List<Submission> list = submissionDAO.findPendingByStationId(stationId);
        for (Submission s : list) {
            s.setItems(itemDAO.findBySubmissionId(s.getId()));
            s.calculateTotalPoints();
        }
        return list;
    }
}
