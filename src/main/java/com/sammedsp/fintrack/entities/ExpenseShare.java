package com.sammedsp.fintrack.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "expense_shares")
public class ExpenseShare extends BaseEntity {

    @Column(name = "root_transaction_id")
    String rootTransactionId;

    String userId;

    Float amount;

    public ExpenseShare(String rootTransactionId, String userId, Float amount) {
        this.rootTransactionId = rootTransactionId;
        this.userId = userId;
        this.amount = amount;
    }
}
