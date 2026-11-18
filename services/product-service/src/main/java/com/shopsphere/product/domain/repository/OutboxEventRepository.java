
package com.shopsphere.product.domain.repository;

import com.shopsphere.product.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByAggregateIdAndStatus(
            String aggregateId,
            String status
    );
}
