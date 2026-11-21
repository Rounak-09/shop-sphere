package com.shopsphere.inventory.exception;

import java.util.UUID;

public class InventoryConcurrencyException extends RuntimeException {

    public InventoryConcurrencyException(UUID productId) {
        super(
                "Inventory was modified by another transaction "
                        + "for product: " + productId
                        + ". Please retry the operation."
        );
    }
}