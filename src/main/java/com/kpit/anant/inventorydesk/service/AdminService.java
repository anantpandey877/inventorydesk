package com.kpit.anant.inventorydesk.service;

import com.kpit.anant.inventorydesk.dto.response.UserResponse;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.enums.AccountStatus;
import com.kpit.anant.inventorydesk.enums.Role;
import com.kpit.anant.inventorydesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingManagers() {
        return userRepository.findByAccountStatus(AccountStatus.PENDING).stream()
                .filter(user -> user.getRole() == Role.INVENTORY_MANAGER)
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public UserResponse approveManager(Long userId) {
        User user = getPendingManager(userId);
        user.setAccountStatus(AccountStatus.APPROVED);
        user.setReviewedAt(LocalDateTime.now());
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse rejectManager(Long userId) {
        User user = getPendingManager(userId);
        user.setAccountStatus(AccountStatus.REJECTED);
        user.setReviewedAt(LocalDateTime.now());
        return toUserResponse(userRepository.save(user));
    }

    private User getPendingManager(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User was not found"));

        if (user.getRole() != Role.INVENTORY_MANAGER) {
            throw new IllegalArgumentException("Only manager accounts can be reviewed");
        }

        if (user.getAccountStatus() != AccountStatus.PENDING) {
            throw new IllegalStateException("Manager account has already been reviewed");
        }

        return user;
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAccountStatus(user.getAccountStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setReviewedAt(user.getReviewedAt());
        return response;
    }
}
