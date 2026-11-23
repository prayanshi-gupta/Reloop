package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.RecyclingStation;
import com.reloop.model.StationSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for recycling stations and aggregated summaries.
 */
public class StationDAO {

    public List<RecyclingStation> findAll() throws DatabaseException {
        List<RecyclingStation> list = new ArrayList<>();
        String sql = "SELECT * FROM recycling_stations ORDER BY name ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching recycling stations: " + e.getMessage(), e);
        }
    }

    public RecyclingStation findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM recycling_stations WHERE id = ?";
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
            throw new DatabaseException("Error finding station by ID: " + e.getMessage(), e);
        }
    }

    public List<StationSummary> getStationSummaries() throws DatabaseException {
        List<StationSummary> list = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.area, " +
                     "COUNT(sub.id) as total_sub, " +
                     "SUM(CASE WHEN sub.status = 'VERIFIED' THEN 1 ELSE 0 END) as verified_sub, " +
                     "COALESCE(SUM(CASE WHEN sub.status = 'VERIFIED' THEN ( " +
                     "    SELECT COALESCE(SUM(si.points_earned), 0) FROM submission_items si WHERE si.submission_id = sub.id " +
                     ") ELSE 0 END), 0) as total_pts " +
                     "FROM recycling_stations s " +
                     "LEFT JOIN submissions sub ON s.id = sub.station_id " +
                     "GROUP BY s.id, s.name, s.area " +
                     "ORDER BY s.name ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new StationSummary(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("area"),
                    rs.getInt("total_sub"),
                    rs.getInt("verified_sub"),
                    rs.getInt("total_pts")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching station summaries: " + e.getMessage(), e);
        }
    }

    private RecyclingStation mapRow(ResultSet rs) throws SQLException {
        return new RecyclingStation(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getString("area"),
            rs.getTime("open_time"),
            rs.getTime("close_time")
        );
    }
}
