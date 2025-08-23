package com.sammedsp.fintrack.dtos;

import java.time.LocalDateTime;

public record ExpenseResponseDto(String id, String remark,String tagId, String tagLabel, Float amount, LocalDateTime time) {
}
