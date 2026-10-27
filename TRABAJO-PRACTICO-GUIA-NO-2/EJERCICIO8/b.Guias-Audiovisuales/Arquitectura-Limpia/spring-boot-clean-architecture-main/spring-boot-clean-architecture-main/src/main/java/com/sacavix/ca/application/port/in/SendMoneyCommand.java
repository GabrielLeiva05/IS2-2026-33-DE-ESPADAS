package com.sacavix.ca.application.port.in;

import java.math.BigDecimal;

public class SendMoneyCommand {
    private final Long sourceId;
    private final Long targetId;
    private final BigDecimal amount;

    public SendMoneyCommand(Long sourceId, Long targetId, BigDecimal amount) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.amount = amount;
    }

    public Long getSourceId() { return sourceId; }
    public Long getTargetId() { return targetId; }
    public BigDecimal getAmount() { return amount; }
}