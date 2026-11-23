package com.reloop.model;

/**
 * Line item in a recycling submission.
 * Records the specific material, verified quantity/weight, and calculated points.
 */
public class SubmissionItem {
    private int id;
    private int submissionId;
    private int materialId;
    private String materialName;
    private MaterialUnit unit;
    private double quantity;
    private int pointsEarned;

    public SubmissionItem() {
    }

    public SubmissionItem(int id, int submissionId, int materialId, String materialName, MaterialUnit unit, double quantity, int pointsEarned) {
        this.id = id;
        this.submissionId = submissionId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
        this.quantity = quantity;
        this.pointsEarned = pointsEarned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public MaterialUnit getUnit() {
        return unit;
    }

    public void setUnit(MaterialUnit unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}
