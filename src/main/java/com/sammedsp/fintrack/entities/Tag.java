package com.sammedsp.fintrack.entities;

import com.sammedsp.fintrack.enums.TagBudgetPeriod;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "tags")
public class Tag extends BaseEntity {
    private String name;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "tag_budget_period", nullable = true)
    private TagBudgetPeriod tagBudgetPeriod;

    @Column(nullable = true)
    private Number budget;

    public Tag(){}

    public Tag(String name, String userId){
        this.name = name;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TagBudgetPeriod getTagBudgetPeriod() {
        return tagBudgetPeriod;
    }

    public void setTagBudgetPeriod(TagBudgetPeriod tagBudgetPeriod) {
        this.tagBudgetPeriod = tagBudgetPeriod;
    }

    public Number getBudget() {
        return budget;
    }

    public void setBudget(Number budget) {
        this.budget = budget;
    }
}
