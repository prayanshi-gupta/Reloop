package com.reloop;

import com.reloop.dao.*;
import com.reloop.exception.DatabaseException;
import com.reloop.model.*;
import com.reloop.server.HttpServerApp;
import com.reloop.util.PasswordUtil;

import java.sql.*;
import java.util.*;

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
        Map<String, Integer> userIds = new LinkedHashMap<>();

        try {
            con.setAutoCommit(false);

            // ---- Customers ----
            String[][] customers = {
                {"Emma Watson", "emma@reloop.com", "customer123", "+1 555-0101"},
                {"John Doe", "john.doe@reloop.com", "customer123", "+1 555-0102"},
                {"Sarah Lee", "sarah.lee@reloop.com", "customer123", "+1 555-0103"},
                {"Mike Chen", "mike.chen@reloop.com", "customer123", "+1 555-0104"},
                {"Lisa Wong", "lisa.wong@reloop.com", "customer123", "+1 555-0105"}
            };
            for (String[] c : customers) {
                User existing = userDAO.findByEmail(c[1]);
                if (existing == null) {
                    String hash = PasswordUtil.hashPassword(c[2]);
                    Customer customer = new Customer(0, c[0], c[1], hash, c[3], null);
                    int id = userDAO.create(customer);
                    userIds.put(c[1], id);
                    walletDAO.create(id, con);
                    System.out.println("[SEED] Customer: " + c[0] + " (" + c[1] + ") id=" + id);
                } else {
                    userIds.put(c[1], existing.getId());
                }
            }

            // ---- Operators (stations 1-5) ----
            String[][] operators = {
                {"Vikram Singh", "vikram@reloop.com", "operator123", "+1 555-0201", "1"},
                {"Priya Nair", "priya@reloop.com", "operator123", "+1 555-0202", "2"},
                {"Ahmed Khan", "ahmed@reloop.com", "operator123", "+1 555-0203", "3"},
                {"Carlos Ruiz", "carlos@reloop.com", "operator123", "+1 555-0204", "4"},
                {"Fatima Ali", "fatima@reloop.com", "operator123", "+1 555-0205", "5"}
            };
            for (String[] o : operators) {
                User existing = userDAO.findByEmail(o[1]);
                if (existing == null) {
                    String hash = PasswordUtil.hashPassword(o[2]);
                    Operator op = new Operator(0, o[0], o[1], hash, o[3], Integer.parseInt(o[4]), null);
                    int id = userDAO.create(op);
                    userIds.put(o[1], id);
                    System.out.println("[SEED] Operator: " + o[0] + " (" + o[1] + ") -> Station " + o[4]);
                } else {
                    userIds.put(o[1], existing.getId());
                }
            }

            // ---- Admin ----
            User adminExisting = userDAO.findByEmail("admin@reloop.com");
            if (adminExisting == null) {
                String hash = PasswordUtil.hashPassword("admin123");
                Admin admin = new Admin(0, "Platform Admin", "admin@reloop.com", hash, "+1 555-0300", null);
                int id = userDAO.create(admin);
                userIds.put("admin@reloop.com", id);
                System.out.println("[SEED] Admin: Platform Admin (admin@reloop.com)");
            } else {
                userIds.put("admin@reloop.com", adminExisting.getId());
            }

                        // ---- Wallet balances ----
            int[] walletBalances = {1250, 840, 620, 310, 150};
            int wi = 0;
            for (String[] c : customers) {
                int custId = userIds.get(c[1]);
                Wallet wallet = walletDAO.findByCustomerId(custId);
                if (wallet != null && wallet.getBalance() == 0) {
                    walletDAO.creditPoints(custId, walletBalances[wi], con);
                    System.out.println("[SEED] Wallet: " + c[0] + " balance=" + walletBalances[wi]);
                }
                wi++;
            }

                        // ---- Submissions, items, transactions ----
            seedSubmissions(con, userIds);

            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            System.out.println("[SEED NOTICE] Demo data already configured or could not be seeded: " + e.getMessage());
        }
    }

    private static void seedSubmissions(Connection con, Map<String, Integer> userIds) throws Exception {
        SubmissionDAO submissionDAO = new SubmissionDAO();
        SubmissionItemDAO itemDAO = new SubmissionItemDAO();
        TransactionDAO txDAO = new TransactionDAO();
        WalletDAO walletDAO = new WalletDAO();

        int emmaId = userIds.get("emma@reloop.com");
        int johnId = userIds.get("john.doe@reloop.com");
        int sarahId = userIds.get("sarah.lee@reloop.com");
        int mikeId = userIds.get("mike.chen@reloop.com");
        int lisaId = userIds.get("lisa.wong@reloop.com");
        int vikramId = userIds.get("vikram@reloop.com");
        int priyaId = userIds.get("priya@reloop.com");
        int ahmedId = userIds.get("ahmed@reloop.com");

        try {
            con.setAutoCommit(false);

            // Sub 1: Emma at Station 1 - VERIFIED - Plastic Bottles 50pcs = 100 pts
            Submission s1 = new Submission();
            s1.setCustomerId(emmaId);
            s1.setStationId(1);
            s1.setStatus(SubmissionStatus.VERIFIED);
            int id1 = submissionDAO.create(s1, con);
            SubmissionItem it1 = new SubmissionItem();
            it1.setSubmissionId(id1);
            it1.setMaterialId(1);
            it1.setQuantity(50.0);
            it1.setMaterialName("Plastic Bottles");
            it1.setUnit(MaterialUnit.PIECE);
            it1.setPointsEarned(100);
            itemDAO.create(it1, con);
            submissionDAO.updateStatus(id1, SubmissionStatus.VERIFIED, vikramId, null, con);
            walletDAO.creditPoints(emmaId, 100, con);
            Wallet w1 = walletDAO.findByCustomerId(emmaId);
            if (w1 != null) txDAO.create(new PointsTransaction(0, w1.getId(), id1, 100, TransactionType.EARNED, null), con);
            System.out.println("[SEED] Submission #" + id1 + " Emma -> Plastic Bottles 50pcs (VERIFIED, +100 pts)");

            // Sub 2: John Doe at Station 2 - VERIFIED - Paper 15kg = 150 pts
            Submission s2 = new Submission();
            s2.setCustomerId(johnId);
            s2.setStationId(2);
            s2.setStatus(SubmissionStatus.VERIFIED);
            int id2 = submissionDAO.create(s2, con);
            SubmissionItem it2 = new SubmissionItem();
            it2.setSubmissionId(id2);
            it2.setMaterialId(3);
            it2.setQuantity(15.0);
            it2.setMaterialName("Paper");
            it2.setUnit(MaterialUnit.KG);
            it2.setPointsEarned(150);
            itemDAO.create(it2, con);
            submissionDAO.updateStatus(id2, SubmissionStatus.VERIFIED, priyaId, null, con);
            walletDAO.creditPoints(johnId, 150, con);
            Wallet w2 = walletDAO.findByCustomerId(johnId);
            if (w2 != null) txDAO.create(new PointsTransaction(0, w2.getId(), id2, 150, TransactionType.EARNED, null), con);
            System.out.println("[SEED] Submission #" + id2 + " John Doe -> Paper 15kg (VERIFIED, +150 pts)");

            // Sub 3: Sarah Lee at Station 3 - VERIFIED - Cardboard 20kg = 160 pts
            Submission s3 = new Submission();
            s3.setCustomerId(sarahId);
            s3.setStationId(3);
            s3.setStatus(SubmissionStatus.VERIFIED);
            int id3 = submissionDAO.create(s3, con);
            SubmissionItem it3 = new SubmissionItem();
            it3.setSubmissionId(id3);
            it3.setMaterialId(4);
            it3.setQuantity(20.0);
            it3.setMaterialName("Cardboard");
            it3.setUnit(MaterialUnit.KG);
            it3.setPointsEarned(160);
            itemDAO.create(it3, con);
            submissionDAO.updateStatus(id3, SubmissionStatus.VERIFIED, ahmedId, null, con);
            walletDAO.creditPoints(sarahId, 160, con);
            Wallet w3 = walletDAO.findByCustomerId(sarahId);
            if (w3 != null) txDAO.create(new PointsTransaction(0, w3.getId(), id3, 160, TransactionType.EARNED, null), con);
            System.out.println("[SEED] Submission #" + id3 + " Sarah Lee -> Cardboard 20kg (VERIFIED, +160 pts)");

            // Sub 4: Mike Chen at Station 4 - PENDING - Plastic Bottles 30pcs
            Submission s4 = new Submission();
            s4.setCustomerId(mikeId);
            s4.setStationId(4);
            s4.setStatus(SubmissionStatus.PENDING);
            int id4 = submissionDAO.create(s4, con);
            SubmissionItem it4 = new SubmissionItem();
            it4.setSubmissionId(id4);
            it4.setMaterialId(1);
            it4.setQuantity(30.0);
            it4.setMaterialName("Plastic Bottles");
            it4.setUnit(MaterialUnit.PIECE);
            it4.setPointsEarned(60);
            itemDAO.create(it4, con);
            System.out.println("[SEED] Submission #" + id4 + " Mike Chen -> Plastic Bottles 30pcs (PENDING)");

            // Sub 5: Lisa Wong at Station 5 - PENDING - E-waste 5pcs
            Submission s5 = new Submission();
            s5.setCustomerId(lisaId);
            s5.setStationId(5);
            s5.setStatus(SubmissionStatus.PENDING);
            int id5 = submissionDAO.create(s5, con);
            SubmissionItem it5 = new SubmissionItem();
            it5.setSubmissionId(id5);
            it5.setMaterialId(8);
            it5.setQuantity(5.0);
            it5.setMaterialName("E-waste");
            it5.setUnit(MaterialUnit.PIECE);
            it5.setPointsEarned(75);
            itemDAO.create(it5, con);
            System.out.println("[SEED] Submission #" + id5 + " Lisa Wong -> E-waste 5pcs (PENDING)");

            // Sub 6: Emma at Station 2 - REJECTED - Aluminium Cans 100pcs
            Submission s6 = new Submission();
            s6.setCustomerId(emmaId);
            s6.setStationId(2);
            s6.setStatus(SubmissionStatus.REJECTED);
            int id6 = submissionDAO.create(s6, con);
            SubmissionItem it6 = new SubmissionItem();
            it6.setSubmissionId(id6);
            it6.setMaterialId(2);
            it6.setQuantity(100.0);
            it6.setMaterialName("Aluminium Cans");
            it6.setUnit(MaterialUnit.PIECE);
            it6.setPointsEarned(300);
            itemDAO.create(it6, con);
            submissionDAO.updateStatus(id6, SubmissionStatus.REJECTED, priyaId, "Contaminated materials - mixed non-recyclables", con);
            System.out.println("[SEED] Submission #" + id6 + " Emma -> Aluminium Cans 100pcs (REJECTED)");

            con.commit();
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }
}
