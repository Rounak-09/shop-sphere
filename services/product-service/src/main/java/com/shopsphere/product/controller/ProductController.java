
package com.shopsphere.product.controller;

import com.shopsphere.product.dto.request.CreateProductRequest;
import com.shopsphere.product.dto.request.UpdateProductRequest;
import com.shopsphere.product.dto.response.PageResponse;
import com.shopsphere.product.dto.response.ProductResponse;

import com.shopsphere.product.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CREATE

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response =
                productService.createProduct(request);

        URI location = URI.create(
                "/api/v1/products/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    // GET BY ID

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(
                productService.getProduct(id)
        );
    }

    // GET ALL / SEARCH

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(name = "query",required = false) String query,
            @RequestParam(name = "category",required = false) String category,
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "20") int size) {

        return ResponseEntity.ok(
                productService.getProducts(
                        query,
                        category,
                        page,
                        size
                )
        );
    }

    // UPDATE

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ResponseEntity.ok(
                productService.updateProduct(id, request)
        );
    }

    // DELETE (SOFT DELETE)

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("id") UUID id) {

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
