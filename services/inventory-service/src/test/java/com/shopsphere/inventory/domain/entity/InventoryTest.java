package com.shopsphere.inventory.domain.entity;

import com.shopsphere.inventory.exception.InsufficientReservedStockException;
import com.shopsphere.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        inventory = new Inventory(productId, "TEST-SKU-001");
    }

    @Test
    void shouldInitializeInventoryWithZeroQuantities() {
        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void shouldAddStock() {
        inventory.addStock(100);

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void shouldReserveStock() {
        inventory.addStock(100);

        inventory.reserveStock(30);

        assertEquals(70, inventory.getAvailableQuantity());
        assertEquals(30, inventory.getReservedQuantity());
    }

    @Test
    void shouldReleaseReservedStock() {
        inventory.addStock(100);
        inventory.reserveStock(30);

        inventory.releaseStock(10);

        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
    }

    @Test
    void shouldThrowExceptionWhenAvailableStockIsInsufficient() {
        inventory.addStock(10);

        assertThrows(
                InsufficientStockException.class,
                () -> inventory.reserveStock(20)
        );
    }

    @Test
    void shouldThrowExceptionWhenReservedStockIsInsufficient() {
        inventory.addStock(10);

        assertThrows(
                InsufficientReservedStockException.class,
                () -> inventory.releaseStock(5)
        );
    }

    @Test
    void shouldRejectZeroOrNegativeStock() {
        assertThrows(IllegalArgumentException.class, () -> inventory.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> inventory.addStock(-5));
    }
}