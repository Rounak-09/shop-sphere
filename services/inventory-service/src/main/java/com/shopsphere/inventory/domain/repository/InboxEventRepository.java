package com.shopsphere.inventory.domain.repository;

import com.shopsphere.inventory.domain.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InboxEventRepository
        extends JpaRepository<InboxEvent, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO inbox_events (
                event_id,
                consumer_name,
                event_type,
                processed_at
            )
            VALUES (
                :eventId,
                :consumerName,
                :eventType,
                CURRENT_TIMESTAMP
            )
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfNotExists(
            @Param("eventId") UUID eventId,
            @Param("consumerName") String consumerName,
            @Param("eventType") String eventType
    );
}