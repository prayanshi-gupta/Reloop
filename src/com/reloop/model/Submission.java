package com.reloop.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer drop-off submission at a recycling station.
 * Holds line items, status (PENDING, VERIFIED, REJECTED), and timestamps.
 */
public class Submission {
    private int id;
    private int customerId;
    private String customerName;
    private int stationId;
    private String stationName;
    private Integer operatorId;
    private String operatorName;
    private SubmissionStatus status;
    private String rejectReason;
    private Timestamp createdAt;
    private Timestamp verifiedAt;
    private int totalPoints;
    private List<SubmissionItem> items = new ArrayList<>();

    public Submission() {
        this.status = SubmissionStatus.PENDING;
    }

    public Submission(int id, int customerId, int stationId, Integer operatorId, SubmissionStatus status, String rejectReason, Timestamp createdAt, Timestamp verifiedAt) {
        this.id = id;
        this.customerId = customerId;
        this.stationId = stationId;
        this.operatorId = operatorId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.createdAt = createdAt;
        this.verifiedAt = verifiedAt;
    }

    public void addItem(SubmissionItem item) {
        this.items.add(item);
    }

    public int calculateTotalPoints() {
        int sum = 0;
        for (SubmissionItem item : items) {
            sum += item.getPointsEarned();
        }
        this.totalPoints = sum;
        return sum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public List<SubmissionItem> getItems() {
        return items;
    }

    public void setItems(List<SubmissionItem> items) {
        this.items = items;
    }
}
