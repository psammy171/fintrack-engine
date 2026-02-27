package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.NotBlank;

public class ResolveSettlementDto {

    @NotBlank
    String userId;

    public String getUserId() {
        return userId;
    }
}
