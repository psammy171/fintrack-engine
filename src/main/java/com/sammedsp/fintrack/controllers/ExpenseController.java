package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.CreateExpenseDto;
import com.sammedsp.fintrack.dtos.ExpenseResponseDto;
import com.sammedsp.fintrack.dtos.PageResponse;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.entities.Expense;
import com.sammedsp.fintrack.exceptions.EntityNotFoundException;
import com.sammedsp.fintrack.services.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    ExpenseController(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(Authentication authentication, @Valid @RequestBody CreateExpenseDto createExpenseDto) throws EntityNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();
        Expense expense = this.expenseService.createExpense(createExpenseDto, userContext);
        return ResponseEntity.ok(expense);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ExpenseResponseDto>> getUserExpenses(Authentication authentication, @RequestParam(defaultValue = "0") int pageNumber,
                                                                            @RequestParam(defaultValue = "50") int pageSize,
                                                                            @RequestParam(required = false) String folderId){
        UserContext userContext = (UserContext) authentication.getPrincipal();

        Sort sort = Sort.by("time").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        PageResponse<ExpenseResponseDto> paginatedExpenses = this.expenseService.getExpense(userContext.userId(), folderId, pageable);
        return ResponseEntity.ok(paginatedExpenses);
    }
}
