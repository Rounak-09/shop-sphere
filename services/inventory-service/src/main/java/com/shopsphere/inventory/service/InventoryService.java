package com.shopsphere.inventory.service;

import com.shopsphere.inventory.domain.entity.Inventory;
import com.shopsphere.inventory.domain.repository.InventoryRepository;
import com.shopsphere.inventory.dto.request.AddStockRequest;
import com.shopsphere.inventory.dto.request.ReleaseStockRequest;
import com.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.shopsphere.inventory.dto.response.InventoryResponse;
import com.shopsphere.inventory.exception.InventoryNotFoundException;
import com.shopsphere.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMapper inventoryMapper
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        return inventoryMapper.toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getBySku(String sku) {

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));

        return inventoryMapper.toResponse(inventory);
    }

    @Transactional
    public InventoryResponse addStock(
            UUID productId,
            AddStockRequest request
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.addStock(request.quantity());

        Inventory updatedInventory = inventoryRepository.saveAndFlush(inventory);

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse reserveStock(
            UUID productId,
            ReserveStockRequest request
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.reserveStock(request.quantity());

        Inventory updatedInventory = inventoryRepository.saveAndFlush(inventory);

        return inventoryMapper.toResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse releaseStock(
            UUID productId,
            ReleaseStockRequest request
    ) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));

        inventory.releaseStock(request.quantity());

        Inventory updatedInventory =
                inventoryRepository.saveAndFlush(inventory);

        return inventoryMapper.toResponse(updatedInventory);
    }
}