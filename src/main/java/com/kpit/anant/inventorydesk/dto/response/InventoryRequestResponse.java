package com.kpit.anant.inventorydesk.dto.response;

import com.kpit.anant.inventorydesk.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestResponse {

    private Long id;
    private String itemName;
    private String sku;
    private String description;
    private String category;
    private Integer initialQuantity;
    private BigDecimal unitPrice;
    private RequestStatus status;
    private InventoryRequestUserResponse requestedBy;
    private InventoryRequestUserResponse reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
