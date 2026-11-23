package com.reloop.handler;

import com.reloop.exception.DatabaseException;
import com.reloop.model.PointsTransaction;
import com.reloop.model.UserSession;
import com.reloop.model.Wallet;
import com.reloop.service.SessionManager;
import com.reloop.service.WalletService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles /api/wallet endpoints: check balance and view transaction history.
 */
public class WalletHandler extends BaseHttpHandler {

    private final WalletService walletService = new WalletService();

    public WalletHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) return;

        UserSession session = authenticate(exchange);
        if (session == null) {
            sendError(exchange, 401, "Authentication required to view wallet");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if (path.endsWith("/balance")) {
                Wallet wallet = walletService.getWalletByCustomerId(session.getUserId());
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("walletId", wallet.getId());
                resp.put("customerId", wallet.getCustomerId());
                resp.put("balance", wallet.getBalance());
                sendSuccess(exchange, resp);
            } else if (path.endsWith("/transactions")) {
                List<PointsTransaction> transactions = walletService.getTransactions(session.getUserId());
                sendSuccess(exchange, transactions);
            } else {
                sendError(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (DatabaseException e) {
            sendError(exchange, 500, "Database error: " + e.getMessage());
        }
    }
}
