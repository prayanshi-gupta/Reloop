package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for Users (Customers, Operators, Admins).
 * Uses PreparedStatement to prevent SQL injection.
 * Demonstrates: JDBC CRUD, Polymorphic mapping to Customer/Operator/Admin subclasses.
 */
public class UserDAO {

    public int create(User user) throws DatabaseException {
        String sql = "INSERT INTO users (name, email, password_hash, role, phone, station_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhone());
            if (user.getStationId() != null && user.getStationId() > 0) {
                ps.setInt(6, user.getStationId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
            throw new DatabaseException("Creating user failed, no ID obtained.");
        } catch (SQLException e) {
            throw new DatabaseException("Database error creating user: " + e.getMessage(), e);
        }
    }

    public User findByEmail(String email) throws DatabaseException {
        String sql = "SELECT u.*, s.name as station_name FROM users u " +
                     "LEFT JOIN recycling_stations s ON u.station_id = s.id " +
                     "WHERE LOWER(u.email) = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    public User findById(int id) throws DatabaseException {
        String sql = "SELECT u.*, s.name as station_name FROM users u " +
                     "LEFT JOIN recycling_stations s ON u.station_id = s.id " +
                     "WHERE u.id = ?";
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
            throw new DatabaseException("Error finding user by ID: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() throws DatabaseException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, s.name as station_name FROM users u " +
                     "LEFT JOIN recycling_stations s ON u.station_id = s.id " +
                     "ORDER BY u.id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching users: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        Role role = Role.fromString(rs.getString("role"));
        String phone = rs.getString("phone");
        int stationIdVal = rs.getInt("station_id");
        Integer stationId = rs.wasNull() ? null : stationIdVal;
        Timestamp createdAt = rs.getTimestamp("created_at");

        if (role == Role.OPERATOR) {
            Operator op = new Operator(id, name, email, passwordHash, phone, stationId, createdAt);
            op.setStationName(rs.getString("station_name"));
            return op;
        } else if (role == Role.ADMIN) {
            return new Admin(id, name, email, passwordHash, phone, createdAt);
        } else {
            return new Customer(id, name, email, passwordHash, phone, createdAt);
        }
    }
}
