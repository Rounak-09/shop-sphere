package com.shopsphere.product.mapper;

import com.shopsphere.product.domain.entity.Product;
import com.shopsphere.product.dto.request.CreateProductRequest;
import com.shopsphere.product.dto.request.UpdateProductRequest;
import com.shopsphere.product.dto.response.ProductResponse;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toEntity(
            CreateProductRequest request) {

        return Product.create(
                request.sku().trim().toUpperCase(),
                request.name().trim(),
                request.description(),
                request.category().trim(),
                request.price(),
                request.currency()
        );
    }

    public static void updateEntity(
            Product product,
            UpdateProductRequest request) {

        product.setSku(request.sku().trim().toUpperCase());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setCurrency(request.currency());
    }

    public static ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getCurrency(),
                product.isActive(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}