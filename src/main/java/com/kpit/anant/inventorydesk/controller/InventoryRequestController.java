package com.kpit.anant.inventorydesk.controller;

import com.kpit.anant.inventorydesk.dto.request.InventoryRequestCreateRequest;
import com.kpit.anant.inventorydesk.dto.response.InventoryRequestResponse;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.service.InventoryRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class InventoryRequestController {

    private final InventoryRequestService inventoryRequestService;

    @PostMapping
    public ResponseEntity<InventoryRequestResponse> createRequest(
            @RequestBody InventoryRequestCreateRequest request,
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryRequestService.createRequest(request, manager));
    }

    @GetMapping("/my")
    public ResponseEntity<List<InventoryRequestResponse>> getMyRequests(
            @AuthenticationPrincipal User manager) {
        return ResponseEntity.ok(inventoryRequestService.getMyRequests(manager));
    }
}
