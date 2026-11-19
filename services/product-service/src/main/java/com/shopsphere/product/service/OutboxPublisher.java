package com.shopsphere.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "shopsphere.outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxBatchProcessor batchProcessor;

    public OutboxPublisher(OutboxBatchProcessor batchProcessor) {
        this.batchProcessor = batchProcessor;
    }

    @Scheduled(
            fixedDelayString =
                    "${shopsphere.outbox.publisher.fixed-delay:5000}",
            initialDelayString = "5000"
    )
    public void publish() {
        try {
            batchProcessor.publishPendingEvents();
        } catch (Exception exception) {
            log.error("Outbox batch processing failed", exception);
        }
    }
}