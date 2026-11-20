package com.shopsphere.inventory.service;

import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.inventory.domain.entity.Inventory;
import com.shopsphere.inventory.domain.repository.InboxEventRepository;
import com.shopsphere.inventory.domain.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryEventProcessor.class);

    private static final String CONSUMER_NAME = "inventory-service";

    private final InboxEventRepository inboxRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryEventProcessor(
            InboxEventRepository inboxRepository,
            InventoryRepository inventoryRepository
    ) {
        this.inboxRepository = inboxRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public void process(ProductEvent event) {

        if (!"PRODUCT_CREATED".equals(event.eventType())) {
            log.info(
                    "Ignoring unsupported product event. eventId={}, eventType={}",
                    event.eventId(),
                    event.eventType()
            );
            return;
        }

        int inserted = inboxRepository.insertIfNotExists(
                event.eventId(),
                CONSUMER_NAME,
                event.eventType()
        );

        if (inserted == 0) {
            log.info(
                    "Duplicate product event ignored. eventId={}",
                    event.eventId()
            );
            return;
        }

        Inventory inventory = new Inventory(
                event.productId(),
                event.sku()
        );

        inventoryRepository.save(inventory);

        log.info(
                "Inventory initialized. productId={}, sku={}",
                event.productId(),
                event.sku()
        );
    }
}