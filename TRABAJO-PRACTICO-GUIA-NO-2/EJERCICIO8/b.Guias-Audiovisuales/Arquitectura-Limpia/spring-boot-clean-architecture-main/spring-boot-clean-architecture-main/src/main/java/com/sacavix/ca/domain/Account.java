package com.sacavix.ca.domain;

import java.math.BigDecimal;

public class Account {
    private Long id;
    private BigDecimal amount;

    public Account(Long id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }

    public Boolean isBalanceGreaterThan(BigDecimal anotherAmount) {
        return this.amount.compareTo(anotherAmount) >= 0;
    }
    public void plus(BigDecimal amount) {
        this.amount = this.amount.add(amount);
    }

    public void subtract(BigDecimal amount) {
        this.amount = this.amount.subtract(amount);
    }
}