package com.shopsphere.product.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        String currency,
        boolean active,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {}