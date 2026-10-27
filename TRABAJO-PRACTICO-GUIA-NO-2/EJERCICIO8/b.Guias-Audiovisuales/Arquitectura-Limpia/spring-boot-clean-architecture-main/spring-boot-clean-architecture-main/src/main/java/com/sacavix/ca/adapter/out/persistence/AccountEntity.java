package com.sacavix.ca.adapter.out.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue
    private Long id;
    private BigDecimal amount;

    public AccountEntity() {}

    public AccountEntity(Long id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}