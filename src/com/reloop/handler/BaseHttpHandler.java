package com.reloop.handler;

import com.reloop.model.UserSession;
import com.reloop.service.SessionManager;
import com.reloop.util.ResponseBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Base HTTP handler offering reusable utilities for JSON I/O, CORS, and auth token extraction.
 * Demonstrates: Inheritance, Stream I/O, Exception handling.
 */
public abstract class BaseHttpHandler implements HttpHandler {

    protected final SessionManager sessionManager;

    public BaseHttpHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    protected void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        sendJsonResponse(exchange, 200, ResponseBuilder.success(data));
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJsonResponse(exchange, statusCode, ResponseBuilder.error(message));
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    protected UserSession authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            return sessionManager.getSession(token);
        }
        return null;
    }

    protected void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    protected boolean handleCorsPreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }
}
