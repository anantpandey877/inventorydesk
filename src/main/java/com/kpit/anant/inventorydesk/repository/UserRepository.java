package com.kpit.anant.inventorydesk.repository;

import com.kpit.anant.inventorydesk.entity.User;
import com.kpit.anant.inventorydesk.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByAccountStatus(AccountStatus status);
}
