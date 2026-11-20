package com.shopsphere.inventory.exception;

import java.util.UUID;

public class InsufficientReservedStockException extends RuntimeException {

    public InsufficientReservedStockException(
            UUID productId,
            int reservedQuantity,
            int requestedQuantity
    ) {
        super(
                "Cannot release stock for product " + productId
                        + ". Reserved: " + reservedQuantity
                        + ", requested: " + requestedQuantity
        );
    }
}