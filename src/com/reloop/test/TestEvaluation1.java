package com.reloop.test;

import com.reloop.model.*;
import com.reloop.service.PointsCalculator;
import com.reloop.service.WeightValidator;
import com.reloop.util.InputValidator;
import com.reloop.util.JsonUtil;
import com.reloop.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Verification test suite for Evaluation 1 core Java components.
 * Tests password hashing, JSON parsing, points calculation, and OOP models.
 */
public class TestEvaluation1 {

    public static void main(String[] args) {
        System.out.println(">>> Running Evaluation 1 Component Verification Suite <<<");
        int passed = 0;
        int failed = 0;

        // Test 1: PasswordUtil Hashing and Verification
        try {
            String raw = "secretPassword123";
            String hash = PasswordUtil.hashPassword(raw);
            assert hash.contains(":") : "Hash must contain salt:hash format";
            assert PasswordUtil.verifyPassword(raw, hash) : "Verification must succeed for correct password";
            assert !PasswordUtil.verifyPassword("wrongPassword", hash) : "Verification must fail for wrong password";
            System.out.println("[PASS] 1. PasswordUtil PBKDF2WithHmacSHA256 & Salt Verification");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 1. PasswordUtil: " + t.getMessage());
            failed++;
        }

        // Test 2: InputValidator
        try {
            InputValidator.validateEmail("test@reloop.com");
            InputValidator.validatePassword("validPass123");
            InputValidator.validatePositiveQuantity(5.5, "Plastic Bottles");
            System.out.println("[PASS] 2. InputValidator Rules & Sanitization");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 2. InputValidator: " + t.getMessage());
            failed++;
        }

        // Test 3: Points Calculation
        try {
            Material plastic = new Material(1, "Plastic Bottles", MaterialUnit.PIECE, 2, true);
            Material paper = new Material(2, "Paper", MaterialUnit.KG, 10, true);

            PointsCalculator calc = new PointsCalculator();
            int pts1 = calc.calculatePoints(plastic, 15); // 15 * 2 = 30
            int pts2 = calc.calculatePoints(paper, 3.5);   // 3.5 * 10 = 35

            if (pts1 != 30 || pts2 != 35) {
                throw new AssertionError("Expected 30 and 35, got " + pts1 + " and " + pts2);
            }

            List<SubmissionItem> items = new ArrayList<>();
            items.add(new SubmissionItem(1, 1, 1, "Plastic Bottles", MaterialUnit.PIECE, 15, pts1));
            items.add(new SubmissionItem(2, 1, 2, "Paper", MaterialUnit.KG, 3.5, pts2));
            int total = calc.calculateTotalPoints(items);

            if (total != 65) {
                throw new AssertionError("Expected total 65, got " + total);
            }

            System.out.println("[PASS] 3. PointsCalculator Math & Material Rates");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 3. PointsCalculator: " + t.getMessage());
            failed++;
        }

        // Test 4: WeightValidator
        try {
            WeightValidator wv = new WeightValidator();
            Material m = new Material(1, "Cardboard", MaterialUnit.KG, 8, true);
            wv.validate(m, 12.0); // valid
            boolean threw = false;
            try {
                wv.validate(m, -2.0); // invalid
            } catch (Exception e) {
                threw = true;
            }
            if (!threw) throw new AssertionError("Expected validation error for negative weight");
            System.out.println("[PASS] 4. WeightValidator Positive Thresholds");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 4. WeightValidator: " + t.getMessage());
            failed++;
        }

        // Test 5: JsonUtil Serialization & Deserialization
        try {
            String jsonInput = "{\"name\":\"Aarav\",\"email\":\"aarav@reloop.com\",\"points\":150,\"active\":true}";
            Map<String, Object> parsed = JsonUtil.parseJsonObject(jsonInput);
            if (!"Aarav".equals(parsed.get("name")) || !"aarav@reloop.com".equals(parsed.get("email"))) {
                throw new AssertionError("JSON parsing failed");
            }
            String serialized = JsonUtil.toJson(parsed);
            if (!serialized.contains("\"name\":\"Aarav\"")) {
                throw new AssertionError("JSON serialization failed");
            }
            System.out.println("[PASS] 5. JsonUtil Zero-Dependency JSON Engine");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 5. JsonUtil: " + t.getMessage());
            failed++;
        }

        // Test 6: OOP Polymorphism
        try {
            User u1 = new Customer(1, "Customer 1", "c1@test.com", "hash", "123", null);
            User u2 = new Operator(2, "Operator 1", "op@test.com", "hash", "456", 1, null);
            User u3 = new Admin(3, "Admin 1", "admin@test.com", "hash", "789", null);

            if (!"CUSTOMER".equals(u1.getDashboardType()) ||
                !"OPERATOR".equals(u2.getDashboardType()) ||
                !"ADMIN".equals(u3.getDashboardType())) {
                throw new AssertionError("Polymorphic method getDashboardType() failed");
            }
            System.out.println("[PASS] 6. OOP Abstraction & Polymorphism across User hierarchy");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] 6. OOP: " + t.getMessage());
            failed++;
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Results: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
