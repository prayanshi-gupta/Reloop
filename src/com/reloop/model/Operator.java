package com.reloop.model;

import java.sql.Timestamp;

/**
 * Concrete Operator class extending User.
 * Assigned to a specific recycling station to inspect and verify materials.
 */
public class Operator extends User {

    private String stationName;

    public Operator() {
        this.role = Role.OPERATOR;
    }

    public Operator(int id, String name, String email, String passwordHash, String phone, Integer stationId, Timestamp createdAt) {
        super(id, name, email, passwordHash, Role.OPERATOR, phone, stationId, createdAt);
    }

    @Override
    public String getDashboardType() {
        return "OPERATOR";
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public boolean hasAssignedStation() {
        return stationId != null && stationId > 0;
    }
}
