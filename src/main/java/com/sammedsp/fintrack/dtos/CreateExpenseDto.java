package com.sammedsp.fintrack.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

public class CreateExpenseDto {

    @NotBlank(message = "Tag Id is required")
    String tagId;

    @NotBlank(message = "Remark is required")
    String remark;

    @NotNull(message = "time is required")
    LocalDateTime time;

    @Positive(message = "Amount should be positive number")
    Float amount;

    String folderId;

    String paidBy;

    @Valid
    List<UserShareDto> userShares;

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

    public String getFolderId() {
        return folderId;
    }

    public String getPaidBy(){return paidBy;}

    public List<UserShareDto> getUserShares() {
        return userShares;
    }
}
