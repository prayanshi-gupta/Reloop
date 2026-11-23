package com.reloop.service;

import com.reloop.exception.ValidationException;
import com.reloop.model.Material;

/**
 * Validates weights and quantities for recyclables before processing.
 */
public class WeightValidator {

    public void validate(Material material, double quantity) throws ValidationException {
        if (material == null) {
            throw new ValidationException("Material cannot be null");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity for " + material.getName() + " must be strictly positive");
        }
        if (quantity > 5000) {
            throw new ValidationException("Quantity for " + material.getName() + " exceeds maximum allowed single-drop limit (5,000)");
        }
    }
}
