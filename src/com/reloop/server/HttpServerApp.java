package com.reloop.server;

import com.reloop.handler.*;
import com.reloop.service.AuthService;
import com.reloop.service.SessionManager;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Built-in standard Java HTTP Server.
 * Configures REST API routes and serves frontend static assets.
 * Demonstrates: com.sun.net.httpserver, Thread pool executor, Routing architecture.
 */
public class HttpServerApp {

    private final int port;
    private HttpServer server;

    public HttpServerApp(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        SessionManager sessionManager = new SessionManager();
        AuthService authService = new AuthService(sessionManager);

        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API context routes
        server.createContext("/api/auth", new AuthHandler(authService, sessionManager));
        server.createContext("/api/stations", new StationHandler(sessionManager));
        server.createContext("/api/materials", new MaterialHandler(sessionManager));
        server.createContext("/api/submissions", new SubmissionHandler(sessionManager));
        server.createContext("/api/wallet", new WalletHandler(sessionManager));
        server.createContext("/api/admin", new AdminHandler(sessionManager));

        // Frontend static file server root
        server.createContext("/", new StaticFileHandler("web"));

        // Multithreaded execution pool for concurrent HTTP requests
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("--------------------------------------------------");
        System.out.println("  RELOOP Smart Recycling & Rewards Platform");
        System.out.println("  Server is listening on: http://localhost:" + port);
        System.out.println("  - Landing Page:        http://localhost:" + port + "/");
        System.out.println("  - Customer Portal:     http://localhost:" + port + "/customer.html");
        System.out.println("  - Operator Portal:     http://localhost:" + port + "/operator.html");
        System.out.println("  - Admin Console:       http://localhost:" + port + "/admin.html");
        System.out.println("--------------------------------------------------");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
