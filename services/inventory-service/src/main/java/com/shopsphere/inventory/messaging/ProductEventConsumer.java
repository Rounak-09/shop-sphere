package com.shopsphere.inventory.messaging;

import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.inventory.service.InventoryEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ProductEventConsumer.class);

    private final InventoryEventProcessor eventProcessor;

    public ProductEventConsumer(
            InventoryEventProcessor eventProcessor
    ) {
        this.eventProcessor = eventProcessor;
    }

    @KafkaListener(
            topics = "shopsphere.product.events",
            groupId = "inventory-service"
    )
    public void consume(ProductEvent event) {

        log.info(
                "Received product event. eventId={}, eventType={}, productId={}",
                event.eventId(),
                event.eventType(),
                event.productId()
        );

        eventProcessor.process(event);
    }
}