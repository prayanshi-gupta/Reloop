package com.reloop.handler;

import com.reloop.dao.MaterialDAO;
import com.reloop.dao.StationDAO;
import com.reloop.exception.DatabaseException;
import com.reloop.model.Material;
import com.reloop.model.RecyclingStation;
import com.reloop.service.SessionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

/**
 * Handles /api/stations endpoints: list stations and station-accepted materials.
 */
public class StationHandler extends BaseHttpHandler {

    private final StationDAO stationDAO = new StationDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();

    public StationHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        try {
            if (query != null && query.contains("stationId=")) {
                int stationId = extractIntParam(query, "stationId");
                List<Material> materials = materialDAO.findByStationId(stationId);
                sendSuccess(exchange, materials);
            } else {
                List<RecyclingStation> stations = stationDAO.findAll();
                sendSuccess(exchange, stations);
            }
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal error: " + e.getMessage());
        }
    }

    private int extractIntParam(String query, String paramName) {
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(paramName)) {
                return Integer.parseInt(kv[1]);
            }
        }
        return -1;
    }
}
