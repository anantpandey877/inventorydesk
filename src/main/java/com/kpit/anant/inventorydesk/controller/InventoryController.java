package com.kpit.anant.inventorydesk.controller;

import com.kpit.anant.inventorydesk.dto.request.StockUpdateRequest;
import com.kpit.anant.inventorydesk.dto.response.InventoryItemResponse;
import com.kpit.anant.inventorydesk.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getActiveItems() {
        return ResponseEntity.ok(inventoryService.getActiveItems());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponse> getActiveItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getActiveItem(itemId));
    }

    @PatchMapping("/{itemId}/stock")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItemResponse> updateStock(
            @PathVariable Long itemId,
            @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(
                inventoryService.updateStock(itemId, request.getStockQuantity()));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<Void> deactivateItem(@PathVariable Long itemId) {
        inventoryService.deactivateItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
