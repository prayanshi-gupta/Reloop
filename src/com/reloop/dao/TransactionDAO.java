package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.PointsTransaction;
import com.reloop.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database logging for points transactions.
 */
public class TransactionDAO {

    public void create(PointsTransaction tx, Connection con) throws SQLException {
        String sql = "INSERT INTO points_transactions (wallet_id, submission_id, points, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getWalletId());
            if (tx.getSubmissionId() != null && tx.getSubmissionId() > 0) {
                ps.setInt(2, tx.getSubmissionId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, tx.getPoints());
            ps.setString(4, tx.getType().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<PointsTransaction> findByWalletId(int walletId) throws DatabaseException {
        List<PointsTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM points_transactions WHERE wallet_id = ? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, walletId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PointsTransaction(
                        rs.getInt("id"),
                        rs.getInt("wallet_id"),
                        rs.getObject("submission_id") != null ? rs.getInt("submission_id") : null,
                        rs.getInt("points"),
                        TransactionType.fromString(rs.getString("type")),
                        rs.getTimestamp("created_at")
                    ));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching transactions: " + e.getMessage(), e);
        }
    }
}
