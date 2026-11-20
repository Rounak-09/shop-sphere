package com.shopsphere.inventory.domain.repository;

import com.shopsphere.inventory.domain.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository
        extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findBySku(String sku);

    boolean existsByProductId(UUID productId);

    boolean existsBySku(String sku);
}