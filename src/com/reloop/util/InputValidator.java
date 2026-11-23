package com.reloop.util;

import com.reloop.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Validates and sanitizes user input (emails, passwords, weights, phone numbers).
 * Demonstrates: Regular expressions, String searching and manipulation, Custom exceptions.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static void validateRegistration(String name, String email, String password, String role) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new ValidationException("Name must be between 2 and 100 characters");
        }
        validateEmail(email);
        validatePassword(password);
        if (role == null || role.trim().isEmpty()) {
            throw new ValidationException("Role is required");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
    }

    public static void validatePositiveQuantity(double quantity, String materialName) throws ValidationException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity for " + materialName + " must be strictly greater than 0");
        }
        if (quantity > 10000) {
            throw new ValidationException("Quantity for " + materialName + " exceeds maximum reasonable limit (10,000)");
        }
    }

    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replace("<", "&lt;").replace(">", "&gt;");
    }
}
