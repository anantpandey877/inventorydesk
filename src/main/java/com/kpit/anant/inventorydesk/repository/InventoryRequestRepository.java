package com.kpit.anant.inventorydesk.repository;

import com.kpit.anant.inventorydesk.entity.InventoryRequest;
import com.kpit.anant.inventorydesk.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {

    List<InventoryRequest> findByRequestedById(Long userId);

    List<InventoryRequest> findByStatus(RequestStatus status);
}
