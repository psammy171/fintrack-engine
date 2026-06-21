package com.sammedsp.fintrack.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ExpensesByDateResponse(LocalDateTime time, Double total, List<ExpenseResponseDto> data) {}
