package com.sacavix.ca.adapter.out.persistence;

import com.sacavix.ca.domain.Account;

public class AccountMapper {

    public static Account entityToDomain(AccountEntity accountEntity) {
        if (accountEntity == null) {
            return null;
        }
        return new Account(
            accountEntity.getId(),
            accountEntity.getAmount()
        );
    }

    public static AccountEntity domainToEntity(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountEntity(
            account.getId(),
            account.getAmount()
        );
    }
}