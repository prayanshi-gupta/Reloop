package com.reloop.service;

import com.reloop.model.Material;
import com.reloop.model.SubmissionItem;

import java.util.List;

/**
 * Calculates reward points earned from submitted recyclable materials.
 * Demonstrates: Encapsulation of business formulas, Math operations.
 */
public class PointsCalculator {

    /**
     * Calculates points for a single material item: quantity * pointsPerUnit.
     */
    public int calculatePoints(Material material, double quantity) {
        if (material == null || quantity <= 0) {
            return 0;
        }
        return (int) Math.round(quantity * material.getPointsPerUnit());
    }

    /**
     * Calculates total points for a list of submission items.
     */
    public int calculateTotalPoints(List<SubmissionItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (SubmissionItem item : items) {
            total += item.getPointsEarned();
        }
        return total;
    }
}
