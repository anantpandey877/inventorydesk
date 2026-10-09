package com.kpit.anant.inventorydesk.service;

import com.kpit.anant.inventorydesk.dto.request.InventoryRequestCreateRequest;
import com.kpit.anant.inventorydesk.dto.response.InventoryRequestResponse;
import com.kpit.anant.inventorydesk.dto.response.InventoryRequestUserResponse;
import com.kpit.anant.inventorydesk.entity.InventoryRequest;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.enums.AccountStatus;
import com.kpit.anant.inventorydesk.enums.Role;
import com.kpit.anant.inventorydesk.repository.InventoryRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InventoryRequestService {

    private final InventoryRequestRepository inventoryRequestRepository;

    @Transactional
    public InventoryRequestResponse createRequest(
            InventoryRequestCreateRequest request,
            User manager) {
        validateManager(manager);
        validateRequest(request);

        InventoryRequest inventoryRequest = InventoryRequest.builder()
                .itemName(request.getItemName())
                .sku(request.getSku())
                .description(request.getDescription())
                .category(request.getCategory())
                .initialQuantity(request.getInitialQuantity())
                .unitPrice(request.getUnitPrice())
                .requestedBy(manager)
                .build();

        return toRequestResponse(inventoryRequestRepository.save(inventoryRequest));
    }

    @Transactional(readOnly = true)
    public List<InventoryRequestResponse> getMyRequests(User manager) {
        validateManager(manager);

        return inventoryRequestRepository.findByRequestedById(manager.getId()).stream()
                .map(this::toRequestResponse)
                .toList();
    }

    private void validateManager(User user) {
        if (user == null
                || user.getRole() != Role.INVENTORY_MANAGER
                || user.getAccountStatus() != AccountStatus.APPROVED) {
            throw new IllegalStateException("Only approved managers can manage inventory requests");
        }
    }

    private void validateRequest(InventoryRequestCreateRequest request) {
        if (request == null
                || isBlank(request.getItemName())
                || isBlank(request.getSku())
                || isBlank(request.getCategory())
                || request.getInitialQuantity() == null
                || request.getInitialQuantity() < 0
                || request.getUnitPrice() == null
                || request.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Item name, SKU, category, quantity, and non-negative price are required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
