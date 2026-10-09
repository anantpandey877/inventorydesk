package com.kpit.anant.inventorydesk.service;

import com.kpit.anant.inventorydesk.dto.response.InventoryItemResponse;
import com.kpit.anant.inventorydesk.entity.InventoryItem;
import com.kpit.anant.inventorydesk.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getActiveItems() {
        return inventoryItemRepository.findByActiveTrue().stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getActiveItem(Long itemId) {
        return toItemResponse(getActiveInventoryItem(itemId));
    }

    @Transactional
    public InventoryItemResponse updateStock(Long itemId, Integer stockQuantity) {
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        InventoryItem item = getActiveInventoryItem(itemId);
        item.setStockQuantity(stockQuantity);
        return toItemResponse(inventoryItemRepository.save(item));
    }

    @Transactional
    public void deactivateItem(Long itemId) {
        InventoryItem item = getActiveInventoryItem(itemId);
        item.setActive(false);
        inventoryItemRepository.save(item);
    }

    private InventoryItem getActiveInventoryItem(Long itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item was not found"));

        if (!Boolean.TRUE.equals(item.getActive())) {
            throw new IllegalStateException("Inventory item is not active");
        }

        return item;
    }

    private InventoryItemResponse toItemResponse(InventoryItem item) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setSku(item.getSku());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setStockQuantity(item.getStockQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setActive(item.getActive());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}
