package com.example.ems.finance.dto;

import java.math.BigDecimal;

public class LineItem {
    private String name;
    private BigDecimal amount;

    public LineItem() {}

    public LineItem(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
