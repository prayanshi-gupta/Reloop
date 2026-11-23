package com.reloop.model;

/**
 * Represents a recyclable material category (e.g. Plastic Bottles, Paper).
 * Encapsulates points per unit and measurement type.
 */
public class Material {
    private int id;
    private String name;
    private MaterialUnit unit;
    private int pointsPerUnit;
    private boolean active;

    public Material() {
    }

    public Material(int id, String name, MaterialUnit unit, int pointsPerUnit, boolean active) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.pointsPerUnit = pointsPerUnit;
        this.active = active;
    }

    /**
     * Calculates points for a given quantity or weight.
     */
    public int calculatePoints(double quantity) {
        if (quantity <= 0) return 0;
        return (int) Math.round(quantity * pointsPerUnit);
    }

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

    public MaterialUnit getUnit() {
        return unit;
    }

    public void setUnit(MaterialUnit unit) {
        this.unit = unit;
    }

    public int getPointsPerUnit() {
        return pointsPerUnit;
    }

    public void setPointsPerUnit(int pointsPerUnit) {
        this.pointsPerUnit = pointsPerUnit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
