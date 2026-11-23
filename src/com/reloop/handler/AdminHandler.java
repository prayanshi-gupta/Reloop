package com.reloop.handler;

import com.reloop.exception.DatabaseException;
import com.reloop.model.Role;
import com.reloop.model.UserSession;
import com.reloop.service.AdminService;
import com.reloop.service.SessionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * Handles /api/admin/overview: platform metrics, registered users, and station activity.
 */
public class AdminHandler extends BaseHttpHandler {

    private final AdminService adminService = new AdminService();

    public AdminHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        UserSession session = authenticate(exchange);
        if (session == null || session.getRole() != Role.ADMIN) {
            sendError(exchange, 403, "Administrator privilege required");
            return;
        }

        try {
            Map<String, Object> data = adminService.getAdminDashboardData();
            sendSuccess(exchange, data);
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        }
    }
}
