package com.sammedsp.fintrack.dtos;

public record ExpenseSummary(TotalExpense total, AverageExpense average, HighestExpense highest, LowestExpense lowest) {}
