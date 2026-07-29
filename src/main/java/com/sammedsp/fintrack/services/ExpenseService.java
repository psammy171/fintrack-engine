package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.*;
import com.sammedsp.fintrack.entities.*;
import com.sammedsp.fintrack.exceptions.BadRequestException;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.repositories.ExpenseRepository;
import com.sammedsp.fintrack.repositories.ExpenseShareRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final FolderService folderService;
    private final ExpenseRepository expenseRepository;
    private final TagService tagService;
    private final ExpenseShareRepository expenseShareRepository;
    private final SettlementService settlementService;

    ExpenseService(ExpenseRepository expenseRepository, TagService tagService, FolderService folderService, ExpenseShareRepository expenseShareRepository, SettlementService settlementService){
        this.expenseRepository = expenseRepository;
        this.tagService = tagService;
        this.folderService = folderService;
        this.expenseShareRepository = expenseShareRepository;
        this.settlementService = settlementService;
    }

    @Transactional
    public ExpenseResponseDto createExpense(CreateExpenseDto createExpenseDto, UserContext userContext) throws EntityNotFoundException {
        String userId = userContext.userId();
        String folderId = createExpenseDto.getFolderId();

        if(folderId != null) {
            return createFolderExpense(createExpenseDto, userId, folderId);
        }

        return createPersonalExpense(createExpenseDto, userId);
    }

    public PageResponse<ExpensesByDateResponse> fetchResponsesByDate(String userId, String folderId, Pageable pageable){
        var expenses = this.getExpense(userId, folderId, pageable);

        var expensesByDate = this.groupExpensesByDate(expenses.content());

        return new PageResponse<>(expensesByDate, expenses.first(), expenses.last(), expenses.totalElements(), expenses.totalPages());
    }

    @Transactional
    public void deleteExpense(String userId, String expenseId) {
        var expense = this.findExpenseByIdAndUserIdOrThrow(expenseId, userId);

        if(expense.getFolderId() != null) {
            var folder = this.folderService.findByIdOrThrow(expense.getFolderId());

            if(folder.isShared()){
                var userShares  = this.expenseShareRepository.findAllByRootTransactionId(expenseId);
                this.settlementService.reverseSettlements(expense.getFolderId(), expense.getUserId(), userShares);
                this.expenseShareRepository.deleteByRootTransactionId(expenseId);
            }
        }

        this.expenseRepository.delete(expense);
    }

    public Expense findExpenseByIdAndUserIdOrThrow(String expenseId, String userId) throws EntityNotFoundException {
        var expense = this.expenseRepository.findByIdAndUserId(expenseId, userId);
        if(expense.isEmpty()){
            throw new EntityNotFoundException(Tag.class.getName(), expenseId);
        }

        return expense.get();
    }

    private PageResponse<ExpenseResponseDto> getExpense(String userId, String folderId, Pageable pageable){

        if(folderId != null){
            var folder = this.folderService.findFolderWithUserAccess(folderId, userId);

            if(folder.isShared()) {
                return this.getSharedFolderExpenses(folderId, pageable);
            }

            return this.getPersonalFolderExpenses(userId, folderId, pageable);
        }

       return this.getRootFolderExpenses(userId, pageable);
    }

    private List<ExpensesByDateResponse> groupExpensesByDate(List<ExpenseResponseDto> expenses) {
        Map<LocalDateTime, List<ExpenseResponseDto>> dateResponseMap = new LinkedHashMap<LocalDateTime, List<ExpenseResponseDto>>();
        for(var expense: expenses){
            var key = expense.time();

            var mapEntry = dateResponseMap.getOrDefault(key, new ArrayList<>());
            mapEntry.add(expense);

            dateResponseMap.put(key, mapEntry);
        }


        var result = new ArrayList<ExpensesByDateResponse>();
        for(var mapEntry: dateResponseMap.entrySet()){
            var time = mapEntry.getKey();
            var timeExpenses = mapEntry.getValue();

            Double total = 0.0;
            for(var exp: timeExpenses){
                total+= exp.amount();
            }

            var expensesByDate = new ExpensesByDateResponse(time, total, timeExpenses);
            result.add(expensesByDate);
        }

        return result;
    }

    private PageResponse<ExpenseResponseDto> getRootFolderExpenses(String userId, Pageable pageable) {

        var expenses = this.expenseRepository.findAllByUserIdAndFolderIdIsNull(userId, pageable);
        var tags = this.tagService.findUserTags(userId);

        var expenseResponses = this.mapExpenseWithTag(expenses, tags);
        return new PageResponse<>(expenseResponses.getContent(),expenseResponses.isFirst(), expenseResponses.isLast(), expenseResponses.getTotalElements(), expenseResponses.getTotalPages());
    }

    private PageResponse<ExpenseResponseDto> getPersonalFolderExpenses(String userId, String folderId, Pageable pageable) {
        var expenses = this.expenseRepository.findAllByFolderId(folderId, pageable);
        var tags = this.tagService.findUserTags(userId);

        var expenseResponses = this.mapExpenseWithTag(expenses, tags);
        return new PageResponse<>(expenseResponses.getContent(),expenseResponses.isFirst(), expenseResponses.isLast(), expenseResponses.getTotalElements(), expenseResponses.getTotalPages());
    }

    private PageResponse<ExpenseResponseDto> getSharedFolderExpenses(String folderId, Pageable pageable) {
        var expenses = this.expenseRepository.findAllByFolderId(folderId, pageable);
        var tags = this.tagService.findSharedFolderTag(folderId);

        var expenseResponses = this.mapExpenseWithTag(expenses, tags);
        return new PageResponse<>(expenseResponses.getContent(),expenseResponses.isFirst(), expenseResponses.isLast(), expenseResponses.getTotalElements(), expenseResponses.getTotalPages());
    }

    private Page<ExpenseResponseDto> mapExpenseWithTag(Page<Expense> expenses, List<Tag> tags) {
        Map<String, Tag> tagsMap = this.mapTagListToMap(tags);

        return expenses.map(expense -> {
            String tagName = null;
            if (expense.getTagId() != null) {
                Tag tag = tagsMap.get(expense.getTagId());
                if (tag != null) {
                    tagName = tag.getName();
                }
            }
            return new ExpenseResponseDto(expense.getId(), expense.getRemark(), expense.getTagId(), tagName,expense.getAmount(), expense.getTime());
        });
    }

    private Map<String, Tag> mapTagListToMap(List<Tag> tags) {
        return tags.stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));
    }

    private ExpenseResponseDto createPersonalExpense(CreateExpenseDto createExpenseDto, String userId) {
        var tag = this.tagService.findTagByIdAndUserIdOrThrow(createExpenseDto.getTagId(), userId);

        var expenseDto = this.createExpenseObject(createExpenseDto, userId);
        Expense expense = this.expenseRepository.save(expenseDto);

        return new ExpenseResponseDto(expense.getId(), expense.getRemark(), expense.getTagId(), tag.getName(), expense.getAmount(), expense.getTime());
    }

    private ExpenseResponseDto createFolderExpense(CreateExpenseDto createExpenseDto, String userId, String folderId) {
        var folder = this.folderService.findByIdOrThrow(folderId);

        if(!folder.isShared() && folder.getUserId().equals(userId)){
            return this.createPersonalExpense(createExpenseDto, userId);
        }

        return this.createSharedFolderExpense(createExpenseDto, userId, folderId);
    }

    private ExpenseResponseDto createSharedFolderExpense(CreateExpenseDto createExpenseDto, String userId, String folderId) {
        var sharedFolderUsers = this.folderService.fetchSharedFolderUserIds(folderId);
        var tag = this.tagService.findByIdAndFolderIdOrThrow(createExpenseDto.getTagId(), folderId);
        this.validateSharedExpense(createExpenseDto, sharedFolderUsers);

        var sharedExpenseDto = this.createExpenseObject(createExpenseDto, createExpenseDto.getPaidBy());
        Expense expense = this.expenseRepository.save(sharedExpenseDto);

        List<ExpenseShare> expenseShares = this.getExpenseShares(createExpenseDto.getUserShares(), expense.getId());
        this.expenseShareRepository.saveAll(expenseShares);

        this.settlementService.updateSettlements(folderId, createExpenseDto.getPaidBy(), createExpenseDto.getUserShares());
        return new ExpenseResponseDto(expense.getId(), expense.getRemark(), expense.getTagId(), tag.getName(), expense.getAmount(), expense.getTime());
    }

    private List<ExpenseShare> getExpenseShares(List<UserShareDto> userShares, String transactionId) {
        List<ExpenseShare> expenseShares = new ArrayList<>();

        for(UserShareDto userShare: userShares){
            var expenseShare = new ExpenseShare(transactionId, userShare.getUserId(), userShare.getAmount());
            expenseShares.add(expenseShare);
        }

        return expenseShares;
    }

    private void validateSharedExpense(CreateExpenseDto createExpenseDto, List<String> sharedFolderUserIds) {
        if(createExpenseDto.getPaidBy() == null) {
            throw new BadRequestException("Paid By is not selected");
        }

        var userShares = createExpenseDto.getUserShares();

        var shareSum = userShares.stream().mapToDouble(UserShareDto::getAmount).sum();

        if(shareSum != createExpenseDto.getAmount()){
            throw new BadRequestException("Invalid expense shares");
        }

        for(UserShareDto userShare: userShares){
            var isValidUser = sharedFolderUserIds.contains(userShare.getUserId());

            if(!isValidUser) {
                throw new BadRequestException("Invalid user id " + userShare.getUserId());
            }
        }

    }

    private Expense createExpenseObject(CreateExpenseDto createExpenseDto, String userId){
        return new Expense(createExpenseDto.getAmount(), createExpenseDto.getRemark(), createExpenseDto.getTagId(), createExpenseDto.getTime(), userId, createExpenseDto.getFolderId());
    }
}
