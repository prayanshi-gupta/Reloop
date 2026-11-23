package com.reloop.model;

/**
 * Recycling submission lifecycle states matching the database ENUM.
 */
public enum SubmissionStatus {
    PENDING,
    VERIFIED,
    REJECTED;

    public static SubmissionStatus fromString(String str) {
        if (str == null) return null;
        try {
            return SubmissionStatus.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
