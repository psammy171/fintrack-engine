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

    private final FolderService folderService;
    private final ExpenseRepository expenseRepository;
    private final TagService tagService;

    ExpenseService(ExpenseRepository expenseRepository, TagService tagService, FolderService folderService){
        this.expenseRepository = expenseRepository;
        this.tagService = tagService;
        this.folderService = folderService;
    }

    public ExpenseResponseDto createExpense(CreateExpenseDto createExpenseDto, UserContext userContext) throws EntityNotFoundException {
        String userId = userContext.userId();
        var tagLabel = this.validateTagAndGetTagLabel(createExpenseDto, userId);
        this.validateFolderId(createExpenseDto, userId);

        Expense expenseDto = this.createExpenseObject(createExpenseDto, userId);
        Expense expense = this.expenseRepository.save(expenseDto);

        return new ExpenseResponseDto(expense.getId(), expense.getRemark(), expense.getTagId(), tagLabel, expense.getAmount(), expense.getTime());
    }

    public PageResponse<ExpenseResponseDto> getExpense(String userId, String folderId, Pageable pageable){
        if(folderId != null){
            this.folderService.checkFolderAccess(folderId, userId);
        }

        Page<Expense> paginatedExpense =  this.expenseRepository.findAllByUserIdAndFolderId(userId, folderId, pageable);

        Map<String, Tag> tagsMap = this.getTagsMap(userId, folderId);

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

    private String validateTagAndGetTagLabel(CreateExpenseDto createExpenseDto, String userId) throws EntityNotFoundException {
        var tagId = createExpenseDto.getTagId();
        var tag = this.tagService.findTagByIdAndUserIdOrThrow(tagId, userId);

        return tag.getName();
    }

    private void validateFolderId(CreateExpenseDto createExpenseDto, String userId) throws EntityNotFoundException {
        var folderId = createExpenseDto.getFolderId();

        if(folderId != null) {
            this.folderService.findByIdAndUserIdOrThrow(createExpenseDto.getFolderId(), userId);
        }
    }

    private Map<String, Tag> getTagsMap(String userId, String folderId){
        List<Tag> tags = this.tagService.getAllTags(userId, folderId, "");
        return tags.stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));
    }

    private Expense createExpenseObject(CreateExpenseDto createExpenseDto, String userId){
        return new Expense(createExpenseDto.getAmount(), createExpenseDto.getRemark(), createExpenseDto.getTagId(), createExpenseDto.getTime(), userId, createExpenseDto.getFolderId());
    }
}
