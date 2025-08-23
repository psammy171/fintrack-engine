package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CurrentMonthExpenseSummary;
import com.sammedsp.fintrack.dtos.DailyExpenseByMonthAnalytics;
import com.sammedsp.fintrack.dtos.DailyExpensesByMonthSummary;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;

    AnalyticsService(ExpenseRepository expenseRepository){
        this.expenseRepository = expenseRepository;
    }

    public CurrentMonthExpenseSummary getCurrentMonthExpenseSummary(String userId){
        return this.expenseRepository.getCurrentMonthExpenseSummary(userId);
    }

    public DailyExpenseByMonthAnalytics getDailyExpensesByMonthSummary(String userId, Optional<Integer> monthParam, Optional<Integer> yearParam) {
        LocalDate now = LocalDate.now();
        Integer month = monthParam.orElse(now.getMonthValue());
        String monthName = Month.of(month).name();
        Integer year = yearParam.orElse(now.getYear());
        List<DailyExpensesByMonthSummary> data = this.expenseRepository.getDailyExpensesByMonthSummary(userId, month, year);
        return new DailyExpenseByMonthAnalytics(monthName, year, this.addDefaultValueForAllDays(data, year, month));
    }

    private List<DailyExpensesByMonthSummary> addDefaultValueForAllDays(List<DailyExpensesByMonthSummary> data, Integer year, Integer month){
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        Map<Integer, Double> dataMap = data.stream()
                .collect(Collectors.toMap(DailyExpensesByMonthSummary::getDay, DailyExpensesByMonthSummary::getTotal));

        List<DailyExpensesByMonthSummary> result = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            Double total = dataMap.getOrDefault(day, 0.0);
            result.add(new DailyExpensesByMonthSummary(total, day));
        }

        return result;
    }
}
