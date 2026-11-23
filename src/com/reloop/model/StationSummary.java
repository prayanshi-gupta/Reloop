package com.reloop.model;

/**
 * Aggregated station statistics DTO for dashboard presentation.
 */
public class StationSummary {
    private int stationId;
    private String stationName;
    private String area;
    private int totalSubmissions;
    private int verifiedSubmissions;
    private int totalPointsDistributed;

    public StationSummary() {
    }

    public StationSummary(int stationId, String stationName, String area, int totalSubmissions, int verifiedSubmissions, int totalPointsDistributed) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.area = area;
        this.totalSubmissions = totalSubmissions;
        this.verifiedSubmissions = verifiedSubmissions;
        this.totalPointsDistributed = totalPointsDistributed;
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

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public int getVerifiedSubmissions() {
        return verifiedSubmissions;
    }

    public void setVerifiedSubmissions(int verifiedSubmissions) {
        this.verifiedSubmissions = verifiedSubmissions;
    }

    public int getTotalPointsDistributed() {
        return totalPointsDistributed;
    }

    public void setTotalPointsDistributed(int totalPointsDistributed) {
        this.totalPointsDistributed = totalPointsDistributed;
    }
}
