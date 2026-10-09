package com.kpit.anant.inventorydesk.service;

import com.kpit.anant.inventorydesk.dto.response.InventoryRequestResponse;
import com.kpit.anant.inventorydesk.dto.response.InventoryRequestUserResponse;
import com.kpit.anant.inventorydesk.entity.InventoryItem;
import com.kpit.anant.inventorydesk.entity.InventoryRequest;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.enums.RequestStatus;
import com.kpit.anant.inventorydesk.enums.Role;
import com.kpit.anant.inventorydesk.repository.InventoryItemRepository;
import com.kpit.anant.inventorydesk.repository.InventoryRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryRequestAdminService {

    private final InventoryRequestRepository inventoryRequestRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional(readOnly = true)
    public List<InventoryRequestResponse> getAllRequests() {
        return inventoryRequestRepository.findAll().stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional
    public InventoryRequestResponse approveRequest(Long requestId, User admin) {
        InventoryRequest request = getPendingRequest(requestId);
        validateAdmin(admin);

        if (inventoryItemRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("An inventory item with this SKU already exists");
        }

        InventoryItem item = InventoryItem.builder()
                .name(request.getItemName())
                .sku(request.getSku())
                .description(request.getDescription())
                .category(request.getCategory())
                .stockQuantity(request.getInitialQuantity())
                .unitPrice(request.getUnitPrice())
                .build();
        inventoryItemRepository.save(item);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        return toRequestResponse(inventoryRequestRepository.save(request));
    }

    @Transactional
    public InventoryRequestResponse rejectRequest(Long requestId, User admin) {
        InventoryRequest request = getPendingRequest(requestId);
        validateAdmin(admin);

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        return toRequestResponse(inventoryRequestRepository.save(request));
    }

    private InventoryRequest getPendingRequest(Long requestId) {
        InventoryRequest request = inventoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory request was not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Inventory request has already been reviewed");
        }

        return request;
    }

    private void validateAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admins can review inventory requests");
        }
    }

    private InventoryRequestResponse toRequestResponse(InventoryRequest request) {
        InventoryRequestResponse response = new InventoryRequestResponse();
        response.setId(request.getId());
        response.setItemName(request.getItemName());
        response.setSku(request.getSku());
        response.setDescription(request.getDescription());
        response.setCategory(request.getCategory());
        response.setInitialQuantity(request.getInitialQuantity());
        response.setUnitPrice(request.getUnitPrice());
        response.setStatus(request.getStatus());
        response.setRequestedBy(toRequestUserResponse(request.getRequestedBy()));
        response.setReviewedBy(toRequestUserResponse(request.getReviewedBy()));
        response.setReviewedAt(request.getReviewedAt());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }

    private InventoryRequestUserResponse toRequestUserResponse(User user) {
        if (user == null) {
            return null;
        }

        InventoryRequestUserResponse response = new InventoryRequestUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAccountStatus(user.getAccountStatus());
        return response;
    }
}
