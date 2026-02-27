package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UserShareDto {
    @NotBlank
    String userId;

    @Positive(message = "Amount should be positive")
    Float amount;

    public String getUserId() {
        return userId;
    }

    public Float getAmount() {
        return amount;
    }
}
