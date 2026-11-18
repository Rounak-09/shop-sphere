
package com.shopsphere.product.service;


import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.product.domain.entity.OutboxEvent;
import com.shopsphere.product.domain.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxService {

    private static final String PRODUCT_TOPIC =
            "shopsphere.product.events";

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxEventRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(ProductEvent event) {

        OutboxEvent outboxEvent = new OutboxEvent(
                event.eventId(),
                "PRODUCT",
                event.productId().toString(),
                event.eventType(),
                PRODUCT_TOPIC,
                objectMapper.valueToTree(event)
        );

        outboxRepository.save(outboxEvent);
    }
}
