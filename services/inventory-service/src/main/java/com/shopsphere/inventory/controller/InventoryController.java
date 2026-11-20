package com.shopsphere.inventory.controller;

import com.shopsphere.inventory.dto.request.AddStockRequest;
import com.shopsphere.inventory.dto.request.ReleaseStockRequest;
import com.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.shopsphere.inventory.dto.response.InventoryResponse;
import com.shopsphere.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(
            @PathVariable("productId") UUID productId
    ) {
        return ResponseEntity.ok(
                inventoryService.getByProductId(productId)
        );
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryResponse> getBySku(
            @PathVariable("sku") String sku
    ) {
        return ResponseEntity.ok(
                inventoryService.getBySku(sku)
        );
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody AddStockRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.addStock(productId, request)
        );
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody ReserveStockRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.reserveStock(productId, request)
        );
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody ReleaseStockRequest request
    ) {
        return ResponseEntity.ok(
                inventoryService.releaseStock(productId, request)
        );
    }
}