package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.ExpenseSummary;
import com.sammedsp.fintrack.dtos.ExpensesByDay;
import com.sammedsp.fintrack.dtos.ExpensesByTagResponse;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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

    @GetMapping("/expenses-by-days")
    public ResponseEntity<List<ExpensesByDay>> getExpensesByDay(Authentication authentication, @RequestParam("start-date") Optional<String> startDate, @RequestParam("end-date") Optional<String> endDate, @RequestParam("folder-id") Optional<String> folderId){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();

        var data = this.analyticsService.getExpensesByDays(userId, startDate, endDate, folderId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/expenses-by-tags")
    public ResponseEntity<List<ExpensesByTagResponse>> getExpensesByTag(Authentication authentication, @RequestParam("start-date") Optional<String> startDate, @RequestParam("end-date") Optional<String> endDate, @RequestParam("folder-id") Optional<String> folderId){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        String userId = userContext.userId();

        var data = this.analyticsService.getExpensesByTags(userId, startDate, endDate, folderId);
        return ResponseEntity.ok(data);
    }
}
