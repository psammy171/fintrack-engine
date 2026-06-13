package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.DailyExpenseByMonthAnalytics;
import com.sammedsp.fintrack.dtos.ExpenseSummary;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummary> getCurrentMonthExpenseSummary(Authentication authentication, @RequestParam("start-date") Optional<String> startDate, @RequestParam("end-date") Optional<String> endDate, @RequestParam("folder-id") Optional<String> folderId){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();

        ExpenseSummary expenseSummary = this.analyticsService.getExpenseSummary(userId, startDate, endDate, folderId);
        return ResponseEntity.ok(expenseSummary);
    }

    @GetMapping("/daily-expenses-by-month")
    public ResponseEntity<DailyExpenseByMonthAnalytics> getDailyExpensesByMonthSummary(Authentication authentication, @RequestParam("year") Optional<Integer> yearParam, @RequestParam("month") Optional<Integer> monthParam){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        DailyExpenseByMonthAnalytics dailyExpensesByMonthSummary = this.analyticsService.getDailyExpensesByMonthSummary(userId, monthParam, yearParam);
        return ResponseEntity.ok(dailyExpensesByMonthSummary);
    }
}
