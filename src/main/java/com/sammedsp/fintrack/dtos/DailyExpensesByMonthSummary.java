package com.sammedsp.fintrack.dtos;

public class DailyExpensesByMonthSummary {
    private Double total;
    private Integer day;

    public DailyExpensesByMonthSummary() {}

    public DailyExpensesByMonthSummary(Double total, Integer day) {
        this.total = total;
        this.day = day;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
}
