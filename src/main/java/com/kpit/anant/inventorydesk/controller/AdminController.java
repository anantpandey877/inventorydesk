package com.kpit.anant.inventorydesk.controller;

import com.kpit.anant.inventorydesk.dto.response.UserResponse;
import com.kpit.anant.inventorydesk.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserResponse>> getPendingManagers() {
        return ResponseEntity.ok(adminService.getPendingManagers());
    }

    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<UserResponse> approveManager(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveManager(userId));
    }

    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<UserResponse> rejectManager(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.rejectManager(userId));
    }
}
