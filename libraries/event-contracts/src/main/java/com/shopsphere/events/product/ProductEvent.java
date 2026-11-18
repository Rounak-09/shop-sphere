
package com.shopsphere.events.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductEvent(
        UUID eventId,
        String eventType,
        UUID productId,
        String sku,
        String name,
        String category,
        BigDecimal price,
        String currency,
        Instant occurredAt
) {
}
