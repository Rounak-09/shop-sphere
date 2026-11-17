package com.shopsphere.product.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "SKU contains invalid characters"
        )
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 150)
        String name,

        @Size(max = 2000)
        String description,

        @NotBlank(message = "Category is required")
        @Size(max = 100)
        String category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency

) {}