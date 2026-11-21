package com.shopsphere.inventory.service;

import com.shopsphere.inventory.domain.entity.Inventory;
import com.shopsphere.inventory.domain.repository.InventoryRepository;
import com.shopsphere.inventory.dto.request.AddStockRequest;
import com.shopsphere.inventory.dto.request.ReleaseStockRequest;
import com.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.shopsphere.inventory.dto.response.InventoryResponse;
import com.shopsphere.inventory.exception.InventoryNotFoundException;
import com.shopsphere.inventory.exception.InsufficientStockException;
import com.shopsphere.inventory.mapper.InventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID productId;
    private Inventory inventory;
    private InventoryResponse expectedResponse;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        inventory = new Inventory(productId, "TEST-SKU-001");

        expectedResponse = new InventoryResponse(
                UUID.randomUUID(),
                productId,
                "TEST-SKU-001",
                100,
                0,
                1L,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void shouldAddStock() {
        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.saveAndFlush(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.toResponse(inventory))
                .thenReturn(expectedResponse);

        InventoryResponse response = inventoryService.addStock(
                productId,
                new AddStockRequest(100)
        );

        assertSame(expectedResponse, response);
        assertEquals(100, inventory.getAvailableQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
        verify(inventoryMapper).toResponse(inventory);
    }

    @Test
    void shouldReserveStock() {
        inventory.addStock(100);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.saveAndFlush(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.toResponse(inventory))
                .thenReturn(expectedResponse);

        InventoryResponse response = inventoryService.reserveStock(
                productId,
                new ReserveStockRequest(30)
        );

        assertSame(expectedResponse, response);
        assertEquals(70, inventory.getAvailableQuantity());
        assertEquals(30, inventory.getReservedQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
    }

    @Test
    void shouldReleaseStock() {
        inventory.addStock(100);
        inventory.reserveStock(30);

        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.saveAndFlush(inventory))
                .thenReturn(inventory);

        when(inventoryMapper.toResponse(inventory))
                .thenReturn(expectedResponse);

        InventoryResponse response = inventoryService.releaseStock(
                productId,
                new ReleaseStockRequest(10)
        );

        assertSame(expectedResponse, response);
        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());

        verify(inventoryRepository).saveAndFlush(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExist() {
        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.addStock(
                        productId,
                        new AddStockRequest(100)
                )
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldNotSaveWhenStockIsInsufficient() {
        when(inventoryRepository.findByProductId(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(
                        productId,
                        new ReserveStockRequest(10)
                )
        );

        verify(inventoryRepository, never()).saveAndFlush(any());
    }
}