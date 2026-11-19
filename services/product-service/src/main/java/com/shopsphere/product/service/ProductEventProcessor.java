package com.shopsphere.product.service;

import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.product.domain.repository.InboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(ProductEventProcessor.class);

    private static final String CONSUMER_NAME = "product-service";

    private final InboxEventRepository inboxRepository;

    public ProductEventProcessor(InboxEventRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @Transactional
    public void process(ProductEvent event) {

        int inserted = inboxRepository.insertIfNotExists(
                event.eventId(),
                CONSUMER_NAME,
                event.eventType()
        );

        if (inserted == 0) {
            log.info(
                    "Duplicate event ignored. eventId={}",
                    event.eventId()
            );
            return;
        }

        // Business processing will be implemented here.
        log.info(
                "Product event processed. eventId={}, eventType={}",
                event.eventId(),
                event.eventType()
        );
    }
}