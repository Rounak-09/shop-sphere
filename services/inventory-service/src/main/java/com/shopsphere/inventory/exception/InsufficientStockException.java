package com.shopsphere.inventory.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(
            UUID productId,
            int availableQuantity,
            int requestedQuantity
    ) {
        super(
                "Insufficient stock for product " + productId
                        + ". Available: " + availableQuantity
                        + ", requested: " + requestedQuantity
        );
    }
}