package com.reloop.model;

/**
 * Points transaction category matching the database ENUM.
 */
public enum TransactionType {
    EARNED,
    REDEEMED;

    public static TransactionType fromString(String str) {
        if (str == null) return null;
        try {
            return TransactionType.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
