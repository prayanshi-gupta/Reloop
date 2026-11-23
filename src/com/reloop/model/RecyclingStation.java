package com.reloop.model;

import java.sql.Time;
import java.time.LocalTime;

/**
 * Represents a physical recycling station kiosk installed near supermarkets.
 */
public class RecyclingStation {
    private int id;
    private String name;
    private String address;
    private String area;
    private Time openTime;
    private Time closeTime;

    public RecyclingStation() {
    }

    public RecyclingStation(int id, String name, String address, String area, Time openTime, Time closeTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.area = area;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * Checks if the station is currently open based on current system time.
     */
    public boolean isOpenNow() {
        if (openTime == null || closeTime == null) return true;
        LocalTime now = LocalTime.now();
        LocalTime open = openTime.toLocalTime();
        LocalTime close = closeTime.toLocalTime();
        return !now.isBefore(open) && !now.isAfter(close);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Time getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Time openTime) {
        this.openTime = openTime;
    }

    public Time getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Time closeTime) {
        this.closeTime = closeTime;
    }
}
