package com.sammedsp.fintrack.dtos;

public class CurrentMonthExpenseSummary {
    private Double total;
    private String month;

    CurrentMonthExpenseSummary(){}

    CurrentMonthExpenseSummary(Double total, String month){
        this.month = month;
        this.total = total;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
