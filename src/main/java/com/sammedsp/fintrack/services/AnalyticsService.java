package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.AverageExpense;
import com.sammedsp.fintrack.dtos.DailyExpenseByMonthAnalytics;
import com.sammedsp.fintrack.dtos.DailyExpensesByMonthSummary;
import com.sammedsp.fintrack.dtos.ExpenseSummary;
import com.sammedsp.fintrack.dtos.ExpenseSummaryQueryResult;
import com.sammedsp.fintrack.dtos.HighestExpense;
import com.sammedsp.fintrack.dtos.LowestExpense;
import com.sammedsp.fintrack.dtos.TopExpenseQueryResult;
import com.sammedsp.fintrack.dtos.TotalExpense;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.utils.DateUtil;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
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

    public ExpenseSummary getExpenseSummary(String userId, Optional<String> startDateString, Optional<String> endDateString, Optional<String> folderId) {
        var startDate = DateUtil.validateAndGetDateString(startDateString, "Start Date");
        var endDate = DateUtil.validateAndGetDateString(endDateString, "End Date");
        
        ExpenseSummaryQueryResult result = this.expenseRepository.fetchExpenseSummary(userId, startDate, endDate, folderId.orElse(null));
        TopExpenseQueryResult highestExpenseResult = this.expenseRepository.fetchHighestExpense(userId, startDate, endDate, folderId.orElse("ROOT"));
        TopExpenseQueryResult lowestExpenseResult = this.expenseRepository.fetchLowestExpense(userId, startDate, endDate, folderId.orElse("ROOT"));

        TotalExpense totalExpense = new TotalExpense(result.total(), startDate, endDate);

        Double average = getAverage(result.total(), startDate, endDate);
        AverageExpense averageExpense = new AverageExpense(average, startDate, endDate);

        HighestExpense highestExpense = castToHighestExpenseSummary(highestExpenseResult);
        LowestExpense lowestExpense = castToLowestExpenseSummary(lowestExpenseResult);

        return new ExpenseSummary(totalExpense, averageExpense, highestExpense, lowestExpense);
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

    private Double getAverage(Double total, String startDate, String endDate) {
        if (total == null) return 0.0;

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        long days = ChronoUnit.DAYS.between(start, end) + 1; // inclusive

        if (days <= 0) return 0.0;

        return total / days;
    }

    private HighestExpense castToHighestExpenseSummary(TopExpenseQueryResult queryResult){
        if(queryResult == null){
            return new HighestExpense(0.0, null);
        }

        return new HighestExpense(queryResult.amount(), queryResult.time());
    }

    private LowestExpense castToLowestExpenseSummary(TopExpenseQueryResult queryResult){
        if(queryResult == null){
            return new LowestExpense(0.0, null);
        }
        
        return new LowestExpense(queryResult.amount(), queryResult.time());
    }
}
