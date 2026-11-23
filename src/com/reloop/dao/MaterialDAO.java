package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.Material;
import com.reloop.model.MaterialUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for recyclable materials.
 */
public class MaterialDAO {

    public List<Material> findAllActive() throws DatabaseException {
        List<Material> list = new ArrayList<>();
        String sql = "SELECT * FROM materials WHERE active = TRUE ORDER BY name ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching active materials: " + e.getMessage(), e);
        }
    }

    public Material findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM materials WHERE id = ?";
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
            throw new DatabaseException("Error finding material by ID: " + e.getMessage(), e);
        }
    }

    public List<Material> findByStationId(int stationId) throws DatabaseException {
        List<Material> list = new ArrayList<>();
        String sql = "SELECT m.* FROM materials m " +
                     "INNER JOIN station_materials sm ON m.id = sm.material_id " +
                     "WHERE sm.station_id = ? AND m.active = TRUE " +
                     "ORDER BY m.name ASC";
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
            throw new DatabaseException("Error fetching station materials: " + e.getMessage(), e);
        }
    }

    private Material mapRow(ResultSet rs) throws SQLException {
        return new Material(
            rs.getInt("id"),
            rs.getString("name"),
            MaterialUnit.fromString(rs.getString("unit")),
            rs.getInt("points_per_unit"),
            rs.getBoolean("active")
        );
    }
}
