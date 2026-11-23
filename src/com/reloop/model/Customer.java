package com.reloop.model;

import java.sql.Timestamp;

/**
 * Concrete Customer class extending User.
 * Demonstrates: Inheritance, Overriding.
 */
public class Customer extends User {

    private int totalSubmissions;
    private int currentPointsBalance;

    public Customer() {
        this.role = Role.CUSTOMER;
    }

    public Customer(int id, String name, String email, String passwordHash, String phone, Timestamp createdAt) {
        super(id, name, email, passwordHash, Role.CUSTOMER, phone, null, createdAt);
    }

    @Override
    public String getDashboardType() {
        return "CUSTOMER";
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public int getCurrentPointsBalance() {
        return currentPointsBalance;
    }

    public void setCurrentPointsBalance(int currentPointsBalance) {
        this.currentPointsBalance = currentPointsBalance;
    }
}
