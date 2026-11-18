package com.shopsphere.product.service;

import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.product.domain.entity.Product;
import com.shopsphere.product.domain.repository.ProductRepository;
import com.shopsphere.product.domain.specification.ProductSpecification;

import com.shopsphere.product.dto.request.CreateProductRequest;
import com.shopsphere.product.dto.request.UpdateProductRequest;
import com.shopsphere.product.dto.response.PageResponse;
import com.shopsphere.product.dto.response.ProductResponse;

import com.shopsphere.product.exception.DuplicateSkuException;
import com.shopsphere.product.exception.ProductNotFoundException;

import com.shopsphere.product.mapper.ProductMapper;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final OutboxService outboxService;

    public ProductService(ProductRepository productRepository, OutboxService outboxService) {
        this.productRepository = productRepository;
        this.outboxService = outboxService;
    }

    // CREATE PRODUCT

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        String sku = request.sku().trim().toUpperCase(Locale.ROOT);

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateSkuException(sku);
        }

        Product product = ProductMapper.toEntity(request);
        product.setSku(sku);

        try {
            Product savedProduct = productRepository.saveAndFlush(product);

            ProductResponse response = ProductMapper.toResponse(savedProduct);

            recordProductEvent("PRODUCT_CREATED", response);

            return response;
        } catch (DataIntegrityViolationException exception) {
            // Protect against concurrent requests with the same SKU.
            throw new DuplicateSkuException(sku);
        }

    }

    // GET PRODUCT BY ID

    public ProductResponse getProduct(UUID id) {

        Product product = productRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductMapper.toResponse(product);
    }

    // GET PRODUCTS WITH SEARCH AND PAGINATION

    public PageResponse<ProductResponse> getProducts(
            String query,
            String category,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Product> productPage =
                productRepository.findAll(
                        ProductSpecification.withFilters(query, category),
                        pageable
                );

        return new PageResponse<>(
                productPage.getContent()
                        .stream()
                        .map(ProductMapper::toResponse)
                        .toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    // UPDATE PRODUCT

    @Transactional
    public ProductResponse updateProduct(
            UUID id,
            UpdateProductRequest request) {

        Product product = productRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String sku = request.sku().trim().toUpperCase();

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(
                sku, id)) {
            throw new DuplicateSkuException(sku);
        }

        ProductMapper.updateEntity(product, request);

        try {
            Product savedProduct = productRepository.saveAndFlush(product);

            ProductResponse response = ProductMapper.toResponse(savedProduct);

            recordProductEvent("PRODUCT_UPDATED", response);

            return response;

        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(sku);
        }
    }

    // SOFT DELETE PRODUCT

    @Transactional
    public void deleteProduct(UUID id) {

        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);

        Product savedProduct = productRepository.saveAndFlush(product);

        ProductResponse response = ProductMapper.toResponse(savedProduct);

        recordProductEvent("PRODUCT_DEACTIVATED", response);
    }

    private void recordProductEvent(
            String eventType,
            ProductResponse product
    ) {
        ProductEvent event = new ProductEvent(
                UUID.randomUUID(),
                eventType,
                product.id(),
                product.sku(),
                product.name(),
                product.category(),
                product.price(),
                product.currency(),
                Instant.now()
        );

        outboxService.record(event);
    }
}