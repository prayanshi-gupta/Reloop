package com.reloop.handler;

import com.reloop.exception.DatabaseException;
import com.reloop.exception.ValidationException;
import com.reloop.model.*;
import com.reloop.service.RecyclingService;
import com.reloop.service.SessionManager;
import com.reloop.service.SubmissionVerifier;
import com.reloop.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles /api/submissions endpoints: create, customer history, pending queue, verify, reject.
 */
public class SubmissionHandler extends BaseHttpHandler {

    private final RecyclingService recyclingService = new RecyclingService();
    private final SubmissionVerifier submissionVerifier = new SubmissionVerifier();

    public SubmissionHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        UserSession session = authenticate(exchange);
        if (session == null) {
            sendError(exchange, 401, "Authentication required to access submissions");
            return;
        }

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/create")) {
                handleCreate(exchange, session);
            } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/customer")) {
                handleCustomerHistory(exchange, session);
            } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/pending")) {
                handlePending(exchange, session);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/verify")) {
                handleVerify(exchange, session);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/reject")) {
                handleReject(exchange, session);
            } else {
                sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (ValidationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Internal error: " + e.getMessage());
        }
    }

    private void handleCreate(HttpExchange exchange, UserSession session) throws IOException, ValidationException, DatabaseException {
        if (session.getRole() != Role.CUSTOMER) {
            sendError(exchange, 403, "Only customers can submit recycling items");
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        int stationId = req.get("stationId") instanceof Number ? ((Number) req.get("stationId")).intValue() : 0;
        List<?> rawItems = (List<?>) req.get("items");

        List<SubmissionItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Object obj : rawItems) {
                if (obj instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) obj;
                    int matId = itemMap.get("materialId") instanceof Number ? ((Number) itemMap.get("materialId")).intValue() : 0;
                    double qty = itemMap.get("quantity") instanceof Number ? ((Number) itemMap.get("quantity")).doubleValue() : 0.0;
                    SubmissionItem it = new SubmissionItem();
                    it.setMaterialId(matId);
                    it.setQuantity(qty);
                    items.add(it);
                }
            }
        }

        Submission created = recyclingService.createSubmission(session.getUserId(), stationId, items);
        sendSuccess(exchange, created);
    }

    private void handleCustomerHistory(HttpExchange exchange, UserSession session) throws IOException, DatabaseException {
        List<Submission> list = recyclingService.getCustomerSubmissions(session.getUserId());
        sendSuccess(exchange, list);
    }

    private void handlePending(HttpExchange exchange, UserSession session) throws IOException, DatabaseException {
        if (session.getRole() != Role.OPERATOR && session.getRole() != Role.ADMIN) {
            sendError(exchange, 403, "Only operators and admins can view the pending queue");
            return;
        }

        int stationId = 0;
        if (session.getStationId() != null) {
            stationId = session.getStationId();
        }

        // Allow query param override for admin testing
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("stationId=")) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && kv[0].equals("stationId")) {
                    stationId = Integer.parseInt(kv[1]);
                }
            }
        }

        if (stationId <= 0) {
            sendError(exchange, 400, "Operator is not assigned to a valid station");
            return;
        }

        List<Submission> pending = recyclingService.getPendingSubmissionsForStation(stationId);
        sendSuccess(exchange, pending);
    }

    private void handleVerify(HttpExchange exchange, UserSession session) throws IOException, ValidationException, DatabaseException {
        if (session.getRole() != Role.OPERATOR && session.getRole() != Role.ADMIN) {
            sendError(exchange, 403, "Only operators can verify submissions");
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        int submissionId = req.get("submissionId") instanceof Number ? ((Number) req.get("submissionId")).intValue() : 0;
        List<?> rawItems = (List<?>) req.get("items");

        List<SubmissionItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Object obj : rawItems) {
                if (obj instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) obj;
                    int matId = itemMap.get("materialId") instanceof Number ? ((Number) itemMap.get("materialId")).intValue() : 0;
                    double qty = itemMap.get("quantity") instanceof Number ? ((Number) itemMap.get("quantity")).doubleValue() : 0.0;
                    SubmissionItem it = new SubmissionItem();
                    it.setMaterialId(matId);
                    it.setQuantity(qty);
                    items.add(it);
                }
            }
        }

        int awardedPoints = submissionVerifier.verifySubmission(submissionId, session.getUserId(), items);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("submissionId", submissionId);
        resp.put("status", "VERIFIED");
        resp.put("pointsAwarded", awardedPoints);
        sendSuccess(exchange, resp);
    }

    private void handleReject(HttpExchange exchange, UserSession session) throws IOException, ValidationException, DatabaseException {
        if (session.getRole() != Role.OPERATOR && session.getRole() != Role.ADMIN) {
            sendError(exchange, 403, "Only operators can reject submissions");
            return;
        }

        String body = readRequestBody(exchange);
        Map<String, Object> req = JsonUtil.parseJsonObject(body);

        int submissionId = req.get("submissionId") instanceof Number ? ((Number) req.get("submissionId")).intValue() : 0;
        String reason = (String) req.get("reason");

        submissionVerifier.rejectSubmission(submissionId, session.getUserId(), reason);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("submissionId", submissionId);
        resp.put("status", "REJECTED");
        resp.put("reason", reason);
        sendSuccess(exchange, resp);
    }
}
