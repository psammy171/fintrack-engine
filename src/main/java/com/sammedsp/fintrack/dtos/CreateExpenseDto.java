package com.sammedsp.fintrack.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class CreateExpenseDto {

    @NotBlank(message = "Tag Id is required")
    String tagId;

    @NotBlank(message = "Remark is required")
    String remark;

    @NotNull(message = "time is required")
    LocalDateTime time;

    @Positive(message = "Amount should be positive number")
    Float amount;

    public String getTagId() {
        return tagId;
    }

    public String getRemark() {
        return remark;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Float getAmount() {
        return amount;
    }
}
