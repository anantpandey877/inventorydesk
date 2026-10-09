package com.kpit.anant.inventorydesk.dto.response;

import com.kpit.anant.inventorydesk.enums.AccountStatus;
import com.kpit.anant.inventorydesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestUserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private AccountStatus accountStatus;
}
