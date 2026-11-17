package com.shopsphere.product.domain.repository;

import com.shopsphere.product.domain.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository
        extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(
            String sku,
            UUID id
    );

    Optional<Product> findByIdAndActiveTrue(UUID id);
}