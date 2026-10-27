package com.sacavix.ca.application.port.out;

import com.sacavix.ca.domain.Account;

public interface LoadAccountPort {
    Account load(Long id);
}