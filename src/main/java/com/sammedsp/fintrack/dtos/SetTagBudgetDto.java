package com.sammedsp.fintrack.dtos;

import com.sammedsp.fintrack.enums.TagBudgetPeriod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SetTagBudgetDto {

    @NotNull(message = "tagBudgetPeriod is required")
    TagBudgetPeriod tagBudgetPeriod;

    @Positive(message = "Budget should be positive")
    Number budget;

    public TagBudgetPeriod getTagBudgetPeriod() {
        return tagBudgetPeriod;
    }

    public Number getBudget() {
        return budget;
    }
}
