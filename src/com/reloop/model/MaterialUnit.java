package com.reloop.model;

/**
 * Material measurement unit matching the database ENUM.
 */
public enum MaterialUnit {
    KG,
    PIECE;

    public static MaterialUnit fromString(String str) {
        if (str == null) return null;
        try {
            return MaterialUnit.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
