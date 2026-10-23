package com.sacavix.ca.application.port.in;

public interface SendMoneyPort {
    public boolean send(SendMoneyCommand command);
}