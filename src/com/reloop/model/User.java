package com.reloop.model;

import java.sql.Timestamp;

/**
 * Abstract base class for every user entity in the RELOOP platform.
 * Demonstrates: Abstraction, Encapsulation, Inheritance.
 */
public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String passwordHash; // stores salt:hash
    protected Role role;
    protected String phone;
    protected Integer stationId; // only for operators
    protected Timestamp createdAt;

    public User() {
    }

    public User(int id, String name, String email, String passwordHash, Role role, String phone, Integer stationId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.phone = phone;
        this.stationId = stationId;
        this.createdAt = createdAt;
    }

    // Abstract method: demonstrates Polymorphism when invoked on User reference
    public abstract String getDashboardType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
