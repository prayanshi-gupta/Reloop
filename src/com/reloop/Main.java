package com.reloop;

import com.reloop.dao.DBConnection;
import com.reloop.dao.UserDAO;
import com.reloop.dao.WalletDAO;
import com.reloop.exception.DatabaseException;
import com.reloop.model.Admin;
import com.reloop.model.Customer;
import com.reloop.model.Operator;
import com.reloop.model.User;
import com.reloop.server.HttpServerApp;
import com.reloop.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Main application entry point for RELOOP.
 * Verifies database connection, seeds initial demo accounts if not present,
 * and launches the embedded Java HTTP Server.
 * Demonstrates: try-with-resources, Exception handling, Multi-tier startup coordination.
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   RELOOP: Smart Recycling & Rewards Platform     ");
        System.out.println("   College Evaluation 1 - Initial Prototype      ");
        System.out.println("==================================================");

        // 1. Verify MySQL connectivity
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("[DB] Connected successfully to MySQL: " + con.getMetaData().getURL());

            // 2. Ensure default demo accounts exist for immediate viva demonstration
            seedDemoUsers(con);

        } catch (DatabaseException | SQLException e) {
            System.err.println("[DB ERROR] Could not connect to MySQL database.");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Please ensure:");
            System.err.println("  1. MySQL Server is running on localhost:3306");
            System.err.println("  2. 'reloop' database has been created using: database/schema.sql");
            System.err.println("  3. Credentials in 'config/db.properties' are correct.");
            System.exit(1);
        }

        // 3. Start Java Built-in HTTP Server
        try {
            int port = DEFAULT_PORT;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {}
            }
            HttpServerApp app = new HttpServerApp(port);
            app.start();
        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Seeds initial test accounts if they do not exist, allowing seamless login testing.
     */
    private static void seedDemoUsers(Connection con) {
        UserDAO userDAO = new UserDAO();
        WalletDAO walletDAO = new WalletDAO();

        try {
            // Demo Customer: customer@reloop.com / customer123
            User cust = userDAO.findByEmail("customer@reloop.com");
            if (cust == null) {
                String hash = PasswordUtil.hashPassword("customer123");
                Customer newCust = new Customer(0, "Aarav Sharma", "customer@reloop.com", hash, "9876543210", null);
                int custId = userDAO.create(newCust);
                walletDAO.create(custId, con);
                System.out.println("[SEED] Created demo customer: customer@reloop.com (password: customer123)");
            }

            // Demo Operator: operator@reloop.com / operator123 assigned to Station 1
            User op = userDAO.findByEmail("operator@reloop.com");
            if (op == null) {
                String hash = PasswordUtil.hashPassword("operator123");
                Operator newOp = new Operator(0, "Vikram Singh", "operator@reloop.com", hash, "9876543211", 1, null);
                userDAO.create(newOp);
                System.out.println("[SEED] Created demo operator: operator@reloop.com (password: operator123, Station 1)");
            }

            // Demo Admin: admin@reloop.com / admin123
            User admin = userDAO.findByEmail("admin@reloop.com");
            if (admin == null) {
                String hash = PasswordUtil.hashPassword("admin123");
                Admin newAdmin = new Admin(0, "Platform Admin", "admin@reloop.com", hash, "9876543212", null);
                userDAO.create(newAdmin);
                System.out.println("[SEED] Created demo admin: admin@reloop.com (password: admin123)");
            }
        } catch (Exception e) {
            System.out.println("[SEED NOTICE] Demo accounts already configured or could not be seeded: " + e.getMessage());
        }
    }
}
