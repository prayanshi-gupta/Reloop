package com.reloop.handler;

import com.reloop.dao.MaterialDAO;
import com.reloop.exception.DatabaseException;
import com.reloop.model.Material;
import com.reloop.service.SessionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

/**
 * Handles /api/materials: returns all active recyclable materials with their points-per-unit.
 */
public class MaterialHandler extends BaseHttpHandler {

    private final MaterialDAO materialDAO = new MaterialDAO();

    public MaterialHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        try {
            List<Material> materials = materialDAO.findAllActive();
            sendSuccess(exchange, materials);
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        }
    }
}
