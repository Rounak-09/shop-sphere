
package com.shopsphere.inventory;

import com.shopsphere.inventory.domain.entity.Inventory;
import com.shopsphere.inventory.domain.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;


import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.kafka.listener.auto-startup=false"
)
class InventoryConcurrencyIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("inventory_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        Inventory inventory = new Inventory(productId, "CONCURRENT-TEST-001");
        inventory.addStock(10);

        inventoryRepository.saveAndFlush(inventory);
    }

    @Test
    void shouldPreventOversellingDuringConcurrentReservations() throws Exception {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Callable<Boolean> reservationTask = () -> {
                TransactionTemplate transactionTemplate =
                        new TransactionTemplate(transactionManager);

                try {
                    transactionTemplate.execute(status -> {

                        Inventory inventory = inventoryRepository
                                .findByProductId(productId)
                                .orElseThrow();

                        // Both transactions must read before either writes.
                        ready.countDown();
                        awaitStart(start);

                        inventory.reserveStock(8);

                        inventoryRepository.saveAndFlush(inventory);

                        return null;
                    });

                    return true;

                } catch (OptimisticLockingFailureException exception) {
                    return false;
                }
            };

            Future<Boolean> first = executor.submit(reservationTask);
            Future<Boolean> second = executor.submit(reservationTask);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both transactions should load inventory"
            );

            // Allow both transactions to attempt their reservation.
            start.countDown();

            boolean firstSucceeded = first.get(15, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(15, TimeUnit.SECONDS);

            int successfulReservations =
                    (firstSucceeded ? 1 : 0) + (secondSucceeded ? 1 : 0);

            int failedReservations =
                    (firstSucceeded ? 0 : 1) + (secondSucceeded ? 0 : 1);

            assertEquals(1, successfulReservations);
            assertEquals(1, failedReservations);

        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        Inventory finalInventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow();

        assertEquals(2, finalInventory.getAvailableQuantity());
        assertEquals(8, finalInventory.getReservedQuantity());
    }

    private void awaitStart(CountDownLatch start) {
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting to start reservation"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Reservation thread was interrupted",
                    exception
            );
        }
    }
}
