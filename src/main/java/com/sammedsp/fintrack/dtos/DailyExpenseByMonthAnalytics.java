package com.sammedsp.fintrack.dtos;

import java.util.List;

public record DailyExpenseByMonthAnalytics(String month, Integer year, List<DailyExpensesByMonthSummary> data) {
}
