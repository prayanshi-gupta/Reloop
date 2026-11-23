package com.reloop.model;

import java.time.Instant;

/**
 * Represents an authenticated in-memory user session.
 */
public class UserSession {
    private String token;
    private int userId;
    private String name;
    private String email;
    private Role role;
    private Integer stationId;
    private Instant createdAt;
    private Instant expiresAt;

    public UserSession(String token, int userId, String name, String email, Role role, Integer stationId, long ttlSeconds) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.stationId = stationId;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(ttlSeconds);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Integer getStationId() {
        return stationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
