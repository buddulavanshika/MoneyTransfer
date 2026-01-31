package com.mts.support;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.util.Money;
import com.mts.domain.model.Account;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Utility to build consistent test data across test classes.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    public static final Currency USD = Currency.getInstance("USD");

    public static Money money(double amount) {
        return Money.of(amount, USD);
    }

    public static Money money(BigDecimal amount) {
        return Money.of(amount, USD);
    }

    public static Account activeAccount(Long id, BigDecimal balance) {
        return buildAccount(id, "ACC-" + id, balance, AccountStatus.ACTIVE);
    }

    public static Account lockedAccount(Long id, BigDecimal balance) {
        return buildAccount(id, "ACC-" + id, balance, AccountStatus.LOCKED);
    }

    public static Account closedAccount(Long id, BigDecimal balance) {
        return buildAccount(id, "ACC-" + id, balance, AccountStatus.CLOSED);
    }

    public static Account buildAccount(Long id, String holderName, BigDecimal balance, AccountStatus status) {
        Account a = new Account();
        a.setId(id);
        a.setHolderName(holderName);
        a.setBalance(balance);
        a.setStatus(status);
        return a;
    }
}
``
