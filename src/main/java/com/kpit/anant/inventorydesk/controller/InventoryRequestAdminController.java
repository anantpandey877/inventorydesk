package com.kpit.anant.inventorydesk.controller;

import com.kpit.anant.inventorydesk.dto.response.InventoryRequestResponse;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.service.InventoryRequestAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
public class InventoryRequestAdminController {

    private final InventoryRequestAdminService inventoryRequestAdminService;

    @GetMapping
    public ResponseEntity<List<InventoryRequestResponse>> getAllRequests() {
        return ResponseEntity.ok(inventoryRequestAdminService.getAllRequests());
    }

    @PatchMapping("/{requestId}/approve")
    public ResponseEntity<InventoryRequestResponse> approveRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(
                inventoryRequestAdminService.approveRequest(requestId, admin));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<InventoryRequestResponse> rejectRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(
                inventoryRequestAdminService.rejectRequest(requestId, admin));
    }
}
