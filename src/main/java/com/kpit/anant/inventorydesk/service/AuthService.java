package com.kpit.anant.inventorydesk.service;

import com.kpit.anant.inventorydesk.dto.request.LoginRequest;
import com.kpit.anant.inventorydesk.dto.request.RegisterRequest;
import com.kpit.anant.inventorydesk.dto.response.AuthResponse;
import com.kpit.anant.inventorydesk.dto.response.UserResponse;
import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.enums.AccountStatus;
import com.kpit.anant.inventorydesk.enums.Role;
import com.kpit.anant.inventorydesk.repository.UserRepository;
import com.kpit.anant.inventorydesk.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null
                || request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Name, email, and password are required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.INVENTORY_MANAGER)
                .accountStatus(AccountStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getAccountStatus() != AccountStatus.APPROVED) {
            throw new IllegalStateException("Account has not been approved");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user), user.getRole());
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
