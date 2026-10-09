package com.kpit.anant.inventorydesk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestCreateRequest {

    private String itemName;
    private String sku;
    private String description;
    private String category;
    private Integer initialQuantity;
    private BigDecimal unitPrice;
}
