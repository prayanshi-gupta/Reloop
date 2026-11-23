package com.reloop.model;

import java.sql.Timestamp;

/**
 * Concrete Admin class extending User.
 * Has administrative access to oversee users, stations, and platform metrics.
 */
public class Admin extends User {

    public Admin() {
        this.role = Role.ADMIN;
    }

    public Admin(int id, String name, String email, String passwordHash, String phone, Timestamp createdAt) {
        super(id, name, email, passwordHash, Role.ADMIN, phone, null, createdAt);
    }

    @Override
    public String getDashboardType() {
        return "ADMIN";
    }
}
