package com.reloop.model;

/**
 * Platform user roles matching the database ENUM.
 */
public enum Role {
    CUSTOMER,
    OPERATOR,
    ADMIN;

    public static Role fromString(String str) {
        if (str == null) return null;
        try {
            return Role.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
