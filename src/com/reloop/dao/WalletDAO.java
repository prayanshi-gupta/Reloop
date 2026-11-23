package com.reloop.dao;

import com.reloop.exception.DatabaseException;
import com.reloop.model.Wallet;

import java.sql.*;

/**
 * Handles database operations for customer wallets.
 * Supports ACID transactions for balance updates.
 */
public class WalletDAO {

    public void create(int customerId, Connection con) throws SQLException {
        String sql = "INSERT INTO wallets (customer_id, balance) VALUES (?, 0)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    public Wallet findByCustomerId(int customerId) throws DatabaseException {
        String sql = "SELECT * FROM wallets WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wallet(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getInt("balance")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding wallet for customer " + customerId + ": " + e.getMessage(), e);
        }
    }

    public void creditPoints(int customerId, int points, Connection con) throws SQLException {
        String sql = "UPDATE wallets SET balance = balance + ? WHERE customer_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                // If wallet row doesn't exist yet, insert it
                create(customerId, con);
                ps.setInt(1, points);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }
        }
    }
}
