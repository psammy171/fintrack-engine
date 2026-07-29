package com.sammedsp.fintrack.repositories;

import com.sammedsp.fintrack.entities.ExpenseShare;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, String> {
    List<ExpenseShare> findAllByRootTransactionId(String transactionId);

    void deleteByRootTransactionId(String transactionId);
}
