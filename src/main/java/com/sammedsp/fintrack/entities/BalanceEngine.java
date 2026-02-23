package com.sammedsp.fintrack.entities;

import jakarta.persistence.Entity;

@Entity(name = "balance_engine")
public class BalanceEngine extends BaseEntity {
    String folderId;

    String creditorId;

    String debitorId;

    Float amount;

    public BalanceEngine(){}

    public BalanceEngine(String folderId, String creditorId, String debitorId, Float amount) {
        this.folderId = folderId;
        this.creditorId = creditorId;
        this.debitorId = debitorId;
        this.amount = amount;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getCreditorId() {
        return creditorId;
    }

    public String getDebitorId() {
        return debitorId;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }
}
