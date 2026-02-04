package com.mts.support;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.model.Account;
import com.mts.domain.util.Money;

import java.math.BigDecimal;
import java.util.Currency;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static final Currency USD = Currency.getInstance("USD");

    public static Money money(double amount) {
        return Money.of(BigDecimal.valueOf(amount), USD);
    }

    public static Money money(BigDecimal amount) {
        return Money.of(amount, USD);
    }

    public static Account activeAccount(Long id, BigDecimal balance) {
        return buildAccount(String.valueOf(id), "ACC-" + id, balance, AccountStatus.ACTIVE);
    }

    public static Account lockedAccount(Long id, BigDecimal balance) {
        return buildAccount(String.valueOf(id), "ACC-" + id, balance, AccountStatus.LOCKED);
    }

    public static Account closedAccount(Long id, BigDecimal balance) {
        return buildAccount(String.valueOf(id), "ACC-" + id, balance, AccountStatus.CLOSED);
    }

    public static Account buildAccount(String id, String holderName, BigDecimal balance, AccountStatus status) {
        // IMPORTANT: pass BigDecimal if Account expects BigDecimal
        return new Account(id, holderName, balance, status);
    }
}