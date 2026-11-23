package com.reloop.dao;

import com.reloop.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles system-wide audit and metric queries for the Admin dashboard.
 */
public class AuditDAO {

    public Map<String, Object> getSystemOverview() throws DatabaseException {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER') as total_customers, " +
                     "(SELECT COUNT(*) FROM users WHERE role = 'OPERATOR') as total_operators, " +
                     "(SELECT COUNT(*) FROM recycling_stations) as total_stations, " +
                     "(SELECT COUNT(*) FROM materials WHERE active = TRUE) as total_materials, " +
                     "(SELECT COUNT(*) FROM submissions) as total_submissions, " +
                     "(SELECT COUNT(*) FROM submissions WHERE status = 'VERIFIED') as verified_submissions, " +
                     "(SELECT COUNT(*) FROM submissions WHERE status = 'PENDING') as pending_submissions, " +
                     "(SELECT COALESCE(SUM(points), 0) FROM points_transactions WHERE type = 'EARNED') as total_points_distributed, " +
                     "(SELECT COALESCE(SUM(si.quantity), 0) FROM submission_items si JOIN submissions s ON si.submission_id = s.id WHERE s.status = 'VERIFIED') as total_recycled_quantity";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalCustomers", rs.getInt("total_customers"));
                stats.put("totalOperators", rs.getInt("total_operators"));
                stats.put("totalStations", rs.getInt("total_stations"));
                stats.put("totalMaterials", rs.getInt("total_materials"));
                stats.put("totalSubmissions", rs.getInt("total_submissions"));
                stats.put("verifiedSubmissions", rs.getInt("verified_submissions"));
                stats.put("pendingSubmissions", rs.getInt("pending_submissions"));
                stats.put("totalPointsDistributed", rs.getInt("total_points_distributed"));
                stats.put("totalRecycledQuantity", rs.getDouble("total_recycled_quantity"));
            }
            return stats;
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating system overview metrics: " + e.getMessage(), e);
        }
    }
}
