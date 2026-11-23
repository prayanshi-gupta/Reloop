package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.Submission;
import com.reloop.model.SubmissionStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for recycling submissions.
 * Supports participating in managed transactions via passing existing Connection.
 */
public class SubmissionDAO {

    public int create(Submission sub, Connection con) throws SQLException {
        String sql = "INSERT INTO submissions (customer_id, station_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sub.getCustomerId());
            ps.setInt(2, sub.getStationId());
            ps.setString(3, sub.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    sub.setId(id);
                    return id;
                }
            }
            throw new SQLException("Creating submission failed, no generated key.");
        }
    }

    public Submission findById(int id) throws DatabaseException {
        String sql = "SELECT s.*, u.name as customer_name, st.name as station_name, op.name as operator_name " +
                     "FROM submissions s " +
                     "JOIN users u ON s.customer_id = u.id " +
                     "JOIN recycling_stations st ON s.station_id = st.id " +
                     "LEFT JOIN users op ON s.operator_id = op.id " +
                     "WHERE s.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding submission by ID: " + e.getMessage(), e);
        }
    }

    public List<Submission> findPendingByStationId(int stationId) throws DatabaseException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name as customer_name, st.name as station_name, NULL as operator_name " +
                     "FROM submissions s " +
                     "JOIN users u ON s.customer_id = u.id " +
                     "JOIN recycling_stations st ON s.station_id = st.id " +
                     "WHERE s.station_id = ? AND s.status = 'PENDING' " +
                     "ORDER BY s.created_at ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching pending submissions: " + e.getMessage(), e);
        }
    }

    public List<Submission> findByCustomerId(int customerId) throws DatabaseException {
        List<Submission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name as customer_name, st.name as station_name, op.name as operator_name " +
                     "FROM submissions s " +
                     "JOIN users u ON s.customer_id = u.id " +
                     "JOIN recycling_stations st ON s.station_id = st.id " +
                     "LEFT JOIN users op ON s.operator_id = op.id " +
                     "WHERE s.customer_id = ? " +
                     "ORDER BY s.id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching customer submissions: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int submissionId, SubmissionStatus status, Integer operatorId, String rejectReason, Connection con) throws SQLException {
        String sql = "UPDATE submissions SET status = ?, operator_id = ?, reject_reason = ?, " +
                     "verified_at = (CASE WHEN ? = 'VERIFIED' THEN CURRENT_TIMESTAMP ELSE NULL END) " +
                     "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (operatorId != null && operatorId > 0) {
                ps.setInt(2, operatorId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, rejectReason);
            ps.setString(4, status.name());
            ps.setInt(5, submissionId);
            ps.executeUpdate();
        }
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission sub = new Submission(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            rs.getInt("station_id"),
            rs.getObject("operator_id") != null ? rs.getInt("operator_id") : null,
            SubmissionStatus.fromString(rs.getString("status")),
            rs.getString("reject_reason"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("verified_at")
        );
        sub.setCustomerName(rs.getString("customer_name"));
        sub.setStationName(rs.getString("station_name"));
        sub.setOperatorName(rs.getString("operator_name"));
        return sub;
    }
}
