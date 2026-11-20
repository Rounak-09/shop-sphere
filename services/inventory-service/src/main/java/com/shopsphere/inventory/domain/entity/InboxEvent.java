package com.shopsphere.inventory.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
public class InboxEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected InboxEvent() {
    }

    public InboxEvent(UUID eventId, String consumerName, String eventType) {
        this.eventId = eventId;
        this.consumerName = consumerName;
        this.eventType = eventType;
        this.processedAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}