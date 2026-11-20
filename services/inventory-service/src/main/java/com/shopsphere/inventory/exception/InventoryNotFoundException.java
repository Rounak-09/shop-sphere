package com.shopsphere.inventory.exception;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(UUID productId) {
        super("Inventory not found for product ID: " + productId);
    }

    public InventoryNotFoundException(String sku) {
        super("Inventory not found for SKU: " + sku);
    }
}