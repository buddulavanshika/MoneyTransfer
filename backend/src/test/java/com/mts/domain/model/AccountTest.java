package com.mts.domain.model;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.util.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static com.mts.support.TestDataFactory.*;

class AccountTest {

    @Test
    @DisplayName("debit(): should reduce balance when sufficient funds")
    void testDebit_Success() {
        Account acc = activeAccount(1L, new BigDecimal("1000.00"));
        Money amount = money(200.00);

        acc.debit(amount);

        assertEquals(0, new BigDecimal("800.00").compareTo(acc.getBalance()),
                "Balance should be 800.00 after debiting 200.00 from 1000.00");
    }

    @Test
    @DisplayName("debit(): should throw InsufficientBalanceException when funds are not enough")
    void testDebit_InsufficientBalance() {
        Account acc = activeAccount(1L, new BigDecimal("500.00"));
        Money amount = money(1200.00);

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () -> acc.debit(amount));
        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("insufficient"),
                "Exception message should indicate insufficient funds");

        // Balance must remain unchanged on failed debit
        assertEquals(0, new BigDecimal("500.00").compareTo(acc.getBalance()));
    }

    @Test
    @DisplayName("credit(): should increase balance correctly")
    void testCredit_Success() {
        Account acc = activeAccount(2L, new BigDecimal("1000.00"));
        Money amount = money(300.00);

        acc.credit(amount);

        assertEquals(0, new BigDecimal("1300.00").compareTo(acc.getBalance()),
                "Balance should be 1300.00 after crediting 300.00 to 1000.00");
    }

    @Test
    @DisplayName("isActive(): returns true only when status is ACTIVE")
    void testIsActive() {
        Account a1 = activeAccount(1L, new BigDecimal("100.00"));
        Account a2 = lockedAccount(2L, new BigDecimal("100.00"));
        Account a3 = closedAccount(3L, new BigDecimal("100.00"));

        assertTrue(a1.isActive());
        assertFalse(a2.isActive());
        assertFalse(a3.isActive());
    }
}