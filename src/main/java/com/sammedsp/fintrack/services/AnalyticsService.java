package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.AverageExpense;
import com.sammedsp.fintrack.dtos.ExpensesByTag;
import com.sammedsp.fintrack.dtos.ExpensesByTagResponse;
import com.sammedsp.fintrack.dtos.ExpenseSummary;
import com.sammedsp.fintrack.dtos.ExpenseSummaryQueryResult;
import com.sammedsp.fintrack.dtos.ExpensesByDay;
import com.sammedsp.fintrack.dtos.HighestExpense;
import com.sammedsp.fintrack.dtos.LowestExpense;
import com.sammedsp.fintrack.dtos.TopExpenseQueryResult;
import com.sammedsp.fintrack.dtos.TotalExpense;
import com.sammedsp.fintrack.entities.Tag;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.utils.DateUtil;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final TagService tagService;

    AnalyticsService(ExpenseRepository expenseRepository, TagService tagService){
        this.expenseRepository = expenseRepository;
        this.tagService = tagService;
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

    public List<ExpensesByDay> getExpensesByDays(String userId, Optional<String> startDateString, Optional<String> endDateString, Optional<String> folderId) {
        var startDate = DateUtil.validateAndGetDateString(startDateString, "Start Date");
        var endDate = DateUtil.validateAndGetDateString(endDateString, "End Date");

        var data =  this.expenseRepository.getExpensesByDays(userId, startDate, endDate, folderId.orElse("ROOT"));
        return addMissingDays(data, startDate, endDate);
    }

    public List<ExpensesByTagResponse> getExpensesByTags(String userId, Optional<String> startDateString, Optional<String> endDateString, Optional<String> folderId){
        var startDate = DateUtil.validateAndGetDateString(startDateString, "Start Date");
        var endDate = DateUtil.validateAndGetDateString(endDateString, "End Date");

        var expensesByTag = this.expenseRepository.getExpensesByTags(userId, startDate, endDate, folderId.orElse("ROOT"));
        var tags = this.tagService.getUsersOrSharedFolderTags(userId, folderId.orElse(null));

        return this.mapTagName(expensesByTag, tags);
    }

    private List<ExpensesByTagResponse> mapTagName(List<ExpensesByTag> expensesByTags, List<Tag> tags){
        Map<String, String> tagNameMap = tags.stream()
            .collect(Collectors.toMap(
                    Tag::getId,
                    Tag::getName
            ));

        var expensesByTagsResponse = new ArrayList<ExpensesByTagResponse>();

        for(var expenseByTag: expensesByTags){
            var tagName = tagNameMap.getOrDefault(expenseByTag.tagId(), "Untitled");
            expensesByTagsResponse.add(new ExpensesByTagResponse(expenseByTag.total(), expenseByTag.tagId(), tagName));
        }

        return expensesByTagsResponse;
    }

    private List<ExpensesByDay> addMissingDays(List<ExpensesByDay> data, String startDate, String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Double> expenseMap = new HashMap<>();
        for (ExpensesByDay d : data) {
            expenseMap.put(d.time(), d.total());
        }

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        List<ExpensesByDay> result = new ArrayList<>();
        while (!start.isAfter(end)) {
            String dateStr = start.format(formatter);
            Double total = expenseMap.getOrDefault(dateStr, 0.0);

            result.add(new ExpensesByDay(total, dateStr));
            start = start.plusDays(1);
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
