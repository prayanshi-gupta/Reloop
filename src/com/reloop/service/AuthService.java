package com.reloop.service;

import com.reloop.dao.DBConnection;
import com.reloop.dao.UserDAO;
import com.reloop.dao.WalletDAO;
import com.reloop.exception.AuthenticationException;
import com.reloop.exception.DatabaseException;
import com.reloop.exception.ValidationException;
import com.reloop.model.*;
import com.reloop.util.InputValidator;
import com.reloop.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles user registration, authentication, and session generation.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final WalletDAO walletDAO = new WalletDAO();
    private final SessionManager sessionManager;

    public AuthService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public UserSession register(String name, String email, String password, String roleStr, String phone, Integer stationId)
            throws ValidationException, DatabaseException, AuthenticationException {

        InputValidator.validateRegistration(name, email, password, roleStr);

        Role role = Role.fromString(roleStr);
        if (role == null) {
            throw new ValidationException("Invalid role specified: " + roleStr);
        }
        if (role == Role.OPERATOR && (stationId == null || stationId <= 0)) {
            throw new ValidationException("Operators must be assigned to a valid recycling station");
        }

        // Check if email already registered
        User existing = userDAO.findByEmail(email);
        if (existing != null) {
            throw new ValidationException("An account with this email already exists");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        User user;
        if (role == Role.CUSTOMER) {
            user = new Customer(0, name.trim(), email.trim(), passwordHash, phone, null);
        } else if (role == Role.OPERATOR) {
            user = new Operator(0, name.trim(), email.trim(), passwordHash, phone, stationId, null);
        } else {
            user = new Admin(0, name.trim(), email.trim(), passwordHash, phone, null);
        }

        // Transactional creation: create user and (if customer) create initial wallet
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int userId = userDAO.create(user);
                user.setId(userId);

                if (role == Role.CUSTOMER) {
                    walletDAO.create(userId, con);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new DatabaseException("Failed to register user: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error during registration: " + e.getMessage(), e);
        }

        return sessionManager.createSession(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStationId());
    }

    public UserSession login(String email, String password) throws AuthenticationException, DatabaseException {
        if (email == null || password == null) {
            throw new AuthenticationException("Email and password are required");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid email or password");
        }

        return sessionManager.createSession(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStationId());
    }
}
