package com.reloop.handler;

import com.reloop.exception.AuthenticationException;
import com.reloop.exception.DatabaseException;
import com.reloop.exception.ValidationException;
import com.reloop.model.UserSession;
import com.reloop.service.AuthService;
import com.reloop.service.SessionManager;
import com.reloop.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles /api/auth endpoints: register, login, me, logout.
 */
public class AuthHandler extends BaseHttpHandler {

    private final AuthService authService;

    public AuthHandler(AuthService authService, SessionManager sessionManager) {
        super(sessionManager);
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/register")) {
                handleRegister(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/login")) {
                handleLogin(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/me")) {
                handleMe(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/logout")) {
                handleLogout(exchange);
            } else {
                sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (AuthenticationException e) {
            sendError(exchange, 401, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException, ValidationException, DatabaseException, AuthenticationException {
        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        String name = (String) req.get("name");
        String email = (String) req.get("email");
        String password = (String) req.get("password");
        String role = (String) req.get("role");
        String phone = (String) req.get("phone");
        Integer stationId = req.get("stationId") instanceof Number ? ((Number) req.get("stationId")).intValue() : null;

        UserSession session = authService.register(name, email, password, role, phone, stationId);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", session.getToken());
        resp.put("userId", session.getUserId());
        resp.put("name", session.getName());
        resp.put("email", session.getEmail());
        resp.put("role", session.getRole().name());
        resp.put("stationId", session.getStationId());

        sendSuccess(exchange, resp);
    }

    private void handleLogin(HttpExchange exchange) throws IOException, AuthenticationException, DatabaseException {
        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        String email = (String) req.get("email");
        String password = (String) req.get("password");

        UserSession session = authService.login(email, password);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", session.getToken());
        resp.put("userId", session.getUserId());
        resp.put("name", session.getName());
        resp.put("email", session.getEmail());
        resp.put("role", session.getRole().name());
        resp.put("stationId", session.getStationId());

        sendSuccess(exchange, resp);
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        UserSession session = authenticate(exchange);
        if (session == null) {
            sendError(exchange, 401, "Not authenticated or session expired");
            return;
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("userId", session.getUserId());
        resp.put("name", session.getName());
        resp.put("email", session.getEmail());
        resp.put("role", session.getRole().name());
        resp.put("stationId", session.getStationId());
        sendSuccess(exchange, resp);
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            sessionManager.removeSession(authHeader.substring(7).trim());
        }
        sendSuccess(exchange, "Logged out successfully");
    }
}
