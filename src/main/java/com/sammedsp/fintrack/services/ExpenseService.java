package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.CreateExpenseDto;
import com.sammedsp.fintrack.dtos.ExpenseResponseDto;
import com.sammedsp.fintrack.dtos.PageResponse;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.entities.Expense;
import com.sammedsp.fintrack.entities.Tag;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TagService tagService;

    ExpenseService(ExpenseRepository expenseRepository, TagService tagService){
        this.expenseRepository = expenseRepository;
        this.tagService = tagService;
    }

    public Expense createExpense(CreateExpenseDto createExpenseDto, UserContext userContext) throws EntityNotFoundException {
        String userId = userContext.userId();
        this.tagService.findTagByIdAndUserIdOrThrow(createExpenseDto.getTagId(), userId);
        Expense expense = this.createExpenseObject(createExpenseDto, userId);
        return this.expenseRepository.save(expense);
    }

    public PageResponse<ExpenseResponseDto> getExpense(String userId, String folderId, Pageable pageable){
        Page<Expense> paginatedExpense =  this.expenseRepository.findAllByUserIdAndFolderId(userId, folderId, pageable);

        Map<String, Tag> tagsMap = this.getTagsMap(userId);

        Page<ExpenseResponseDto> expenseResponseDtoPage =  paginatedExpense.map(expense -> {
            String tagName = null;
            if (expense.getTagId() != null) {
                Tag tag = tagsMap.get(expense.getTagId());
                if (tag != null) {
                    tagName = tag.getName();
                }
            }
            return new ExpenseResponseDto(expense.getId(), expense.getRemark(), expense.getTagId(), tagName,expense.getAmount(), expense.getTime());
        });

        return new PageResponse<ExpenseResponseDto>(expenseResponseDtoPage.getContent(),expenseResponseDtoPage.isFirst(), expenseResponseDtoPage.isLast(), expenseResponseDtoPage.getTotalElements(), expenseResponseDtoPage.getTotalPages());

    }

    private Map<String, Tag> getTagsMap(String userId){
        List<Tag> tags = this.tagService.getAllUserTags(userId);
        return tags.stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));
    }

    private Expense createExpenseObject(CreateExpenseDto createExpenseDto, String userId){
        return new Expense(createExpenseDto.getAmount(), createExpenseDto.getRemark(), createExpenseDto.getTagId(), createExpenseDto.getTime(), userId, createExpenseDto.getFolderId());
    }
}
