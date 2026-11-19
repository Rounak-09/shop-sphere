
package com.shopsphere.product.service;

import com.shopsphere.product.domain.entity.OutboxEvent;
import com.shopsphere.product.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxBatchProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxBatchProcessor.class);

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${shopsphere.outbox.publisher.batch-size:50}")
    private int batchSize;

    @Value("${shopsphere.outbox.publisher.max-retries:5}")
    private int maxRetries;

    public OutboxBatchProcessor(
            OutboxEventRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepository.findPendingEvents(batchSize);

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {
            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    event.getPayload().toString()
            ).get(10, TimeUnit.SECONDS);

            event.markAsPublished();

            log.info(
                    "Published outbox event. eventId={}, eventType={}, aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId()
            );

        } catch (Exception exception) {

            String error = exception.getMessage();

            if (error == null || error.isBlank()) {
                error = exception.getClass().getSimpleName();
            }

            if (error.length() > 2000) {
                error = error.substring(0, 2000);
            }

            event.markAsFailed(error, maxRetries);

            log.error(
                    "Failed to publish outbox event. eventId={}, retryCount={}",
                    event.getId(),
                    event.getRetryCount(),
                    exception
            );
        }
    }
}
