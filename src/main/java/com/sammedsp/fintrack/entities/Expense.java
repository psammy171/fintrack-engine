package com.sammedsp.fintrack.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity(name = "expenses")
public class Expense extends BaseEntity {

    private Float amount;

    private String remark;

    @Column(name = "tag_id")
    private String tagId;

    private LocalDateTime time;

    @Column(name = "user_id")
    private String userId;

    public Expense(){}

    public Expense(Float amount, String remark, String tagId, LocalDateTime time, String userId){
        this.amount = amount;
        this.remark = remark;
        this.tagId = tagId;
        this.time = time;
        this.userId = userId;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
