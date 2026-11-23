package com.reloop.service;

import com.reloop.model.Role;
import com.reloop.model.UserSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory session manager.
 * Demonstrates: Concurrent collections (ConcurrentHashMap), UUID token generation.
 */
public class SessionManager {

    private static final long SESSION_TTL_SECONDS = 86400; // 24 hours
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession createSession(int userId, String name, String email, Role role, Integer stationId) {
        String token = UUID.randomUUID().toString();
        UserSession session = new UserSession(token, userId, name, email, role, stationId, SESSION_TTL_SECONDS);
        sessions.put(token, session);
        return session;
    }

    public UserSession getSession(String token) {
        if (token == null) return null;
        UserSession session = sessions.get(token);
        if (session != null && session.isExpired()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public void removeSession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
