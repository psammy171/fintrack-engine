package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.CurrentMonthExpenseSummary;
import com.sammedsp.fintrack.dtos.DailyExpenseByMonthAnalytics;
import com.sammedsp.fintrack.dtos.DailyExpensesByMonthSummary;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @RequestMapping("/current-month-summary")
    public ResponseEntity<CurrentMonthExpenseSummary> getCurrentMonthExpenseSummary(Authentication authentication){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        CurrentMonthExpenseSummary currentMonthExpenseSummary = this.analyticsService.getCurrentMonthExpenseSummary(userId);
        return ResponseEntity.ok(currentMonthExpenseSummary);
    }

    @RequestMapping("/daily-expenses-by-month")
    public ResponseEntity<DailyExpenseByMonthAnalytics> getDailyExpensesByMonthSummary(Authentication authentication, @RequestParam("year") Optional<Integer> yearParam, @RequestParam("month") Optional<Integer> monthParam){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();
        DailyExpenseByMonthAnalytics dailyExpensesByMonthSummary = this.analyticsService.getDailyExpensesByMonthSummary(userId, monthParam, yearParam);
        return ResponseEntity.ok(dailyExpensesByMonthSummary);
    }
}
