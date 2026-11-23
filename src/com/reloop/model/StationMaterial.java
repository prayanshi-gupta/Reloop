package com.reloop.model;

/**
 * Models the relationship between a station and the materials it accepts.
 * Maps to the database junction table `station_materials`.
 */
public class StationMaterial {
    private int stationId;
    private int materialId;
    private String materialName;
    private MaterialUnit unit;
    private int pointsPerUnit;

    public StationMaterial() {
    }

    public StationMaterial(int stationId, int materialId, String materialName, MaterialUnit unit, int pointsPerUnit) {
        this.stationId = stationId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
        this.pointsPerUnit = pointsPerUnit;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
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

    public int getPointsPerUnit() {
        return pointsPerUnit;
    }

    public void setPointsPerUnit(int pointsPerUnit) {
        this.pointsPerUnit = pointsPerUnit;
    }
}
