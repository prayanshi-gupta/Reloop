package com.reloop.model;

import java.sql.Timestamp;

/**
 * Immutable audit record of a points transaction (EARNED or REDEEMED).
 */
public class PointsTransaction {
    private int id;
    private int walletId;
    private Integer submissionId;
    private int points;
    private TransactionType type;
    private Timestamp createdAt;

    public PointsTransaction() {
    }

    public PointsTransaction(int id, int walletId, Integer submissionId, int points, TransactionType type, Timestamp createdAt) {
        this.id = id;
        this.walletId = walletId;
        this.submissionId = submissionId;
        this.points = points;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
