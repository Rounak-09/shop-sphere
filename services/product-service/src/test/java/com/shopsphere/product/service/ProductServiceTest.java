package com.shopsphere.product.service;

import com.shopsphere.events.product.ProductEvent;
import com.shopsphere.product.domain.entity.Product;
import com.shopsphere.product.domain.repository.ProductRepository;

import com.shopsphere.product.dto.request.CreateProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;

import com.shopsphere.product.exception.DuplicateSkuException;
import com.shopsphere.product.exception.ProductNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProductSuccessfully() {

        CreateProductRequest request =
                new CreateProductRequest(
                        "IPHONE-001",
                        "iPhone 17",
                        "Apple smartphone",
                        "Electronics",
                        new BigDecimal("79999.00"),
                        "INR"
                );

        UUID productId = UUID.randomUUID();

        // Simulate persistence.
        when(productRepository.existsBySkuIgnoreCase(
                "IPHONE-001"
        )).thenReturn(false);

        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);

                    // Simulate the generated database ID.
                    // JPA callbacks are not executed by Mockito.
                    try {
                        var field = Product.class.getDeclaredField("id");
                        field.setAccessible(true);
                        field.set(product, productId);
                    } catch (ReflectiveOperationException exception) {
                        throw new RuntimeException(exception);
                    }

                    return product;
                });

        ProductResponse response =
                productService.createProduct(request);

        assertEquals(productId, response.id());
        assertEquals("IPHONE-001", response.sku());
        assertEquals("iPhone 17", response.name());
        assertEquals(new BigDecimal("79999.00"), response.price());

        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSkuAlreadyExists() {

        CreateProductRequest request =
                new CreateProductRequest(
                        "IPHONE-001",
                        "iPhone 17",
                        "Smartphone",
                        "Electronics",
                        new BigDecimal("79999.00"),
                        "INR"
                );

        when(productRepository.existsBySkuIgnoreCase(
                "IPHONE-001"
        )).thenReturn(true);

        assertThrows(
                DuplicateSkuException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository, never())
                .saveAndFlush(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        UUID productId = UUID.randomUUID();

        when(productRepository.findByIdAndActiveTrue(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(productId)
        );
    }

    @Test
    void shouldUpdateProductSuccessfully() {

        UUID id = UUID.randomUUID();

        Product product = Product.create(
                "OLD-SKU",
                "Old Product",
                "Old description",
                "Electronics",
                new BigDecimal("100.00"),
                "INR"
        );

        var request = new com.shopsphere.product.dto.request
                .UpdateProductRequest(
                "NEW-SKU",
                "Updated Product",
                "Updated description",
                "Electronics",
                new BigDecimal("200.00"),
                "INR"
        );

        when(productRepository.findByIdAndActiveTrue(id))
                .thenReturn(Optional.of(product));

        when(productRepository.existsBySkuIgnoreCaseAndIdNot(
                "NEW-SKU", id
        )).thenReturn(false);

        when(productRepository.saveAndFlush(product))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProduct(id, request);

        assertEquals("NEW-SKU", response.sku());
        assertEquals("Updated Product", response.name());
        assertEquals(new BigDecimal("200.00"), response.price());
    }

    @Test
    void shouldDeactivateProductSuccessfully() {

        UUID id = UUID.randomUUID();

        Product product = Product.create(
                "IPHONE-001",
                "iPhone 17",
                "Apple smartphone",
                "Electronics",
                new BigDecimal("79999.00"),
                "INR"
        );
        product.setActive(true);

        when(productRepository.findByIdAndActiveTrue(id))
                .thenReturn(Optional.of(product));

        // Mockito should return the same entity after saving.
        when(productRepository.saveAndFlush(product))
                .thenReturn(product);

        productService.deleteProduct(id);

        assertFalse(product.isActive());

        verify(productRepository).saveAndFlush(product);

        verify(outboxService).record(any(ProductEvent.class));
    }
}