package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.MaterialUnit;
import com.reloop.model.SubmissionItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for individual submission line items.
 */
public class SubmissionItemDAO {

    public void create(SubmissionItem item, Connection con) throws SQLException {
        String sql = "INSERT INTO submission_items (submission_id, material_id, quantity, points_earned) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getSubmissionId());
            ps.setInt(2, item.getMaterialId());
            ps.setDouble(3, item.getQuantity());
            ps.setInt(4, item.getPointsEarned());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<SubmissionItem> findBySubmissionId(int submissionId) throws DatabaseException {
        List<SubmissionItem> list = new ArrayList<>();
        String sql = "SELECT si.*, m.name as material_name, m.unit as material_unit " +
                     "FROM submission_items si " +
                     "JOIN materials m ON si.material_id = m.id " +
                     "WHERE si.submission_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SubmissionItem(
                        rs.getInt("id"),
                        rs.getInt("submission_id"),
                        rs.getInt("material_id"),
                        rs.getString("material_name"),
                        MaterialUnit.fromString(rs.getString("material_unit")),
                        rs.getDouble("quantity"),
                        rs.getInt("points_earned")
                    ));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching submission items: " + e.getMessage(), e);
        }
    }

    public void deleteBySubmissionId(int submissionId, Connection con) throws SQLException {
        String sql = "DELETE FROM submission_items WHERE submission_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, submissionId);
            ps.executeUpdate();
        }
    }
}
