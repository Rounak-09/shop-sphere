package com.shopsphere.inventory.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID productId,
        String sku,
        int availableQuantity,
        int reservedQuantity,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}