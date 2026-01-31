package com.mts.domain.model;

import com.mts.domain.enums.AccountStatus;
import com.mts.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    @DisplayName("Constructor should normalize opening balance to scale=2 (HALF_UP) and default to ACTIVE when using convenience ctor")
    void constructorNormalizesAndDefaultsActive() {
        Account a = new Account("A-1", "Alice", new BigDecimal("100.1")); // -> 100.10
        assertEquals("A-1", a.getId());
        assertEquals("Alice", a.getHolderName());
        assertEquals(AccountStatus.ACTIVE, a.getStatus());
        assertEquals(0, a.getBalance().compareTo(new BigDecimal("100.10")));
    }

    @Test
    @DisplayName("Constructor with explicit status should set status and normalize opening balance")
    void constructorWithStatus() {
        Account a = new Account("B-1", "Bob", new BigDecimal("50.555"), AccountStatus.LOCKED); // -> 50.56
        assertEquals(AccountStatus.LOCKED, a.getStatus());
        assertEquals(0, a.getBalance().compareTo(new BigDecimal("50.56")));
    }

    @Test
    @DisplayName("credit(BigDecimal) should increase balance and normalize to 2 decimals")
    void creditIncreasesBalance() {
        Account a = new Account("C-1", "Carol", new BigDecimal("10.00")); // ACTIVE

        a.credit(new BigDecimal("2.345")); // normalize to 2.35
        assertEquals(0, a.getBalance().compareTo(new BigDecimal("12.35")));
    }

    @Test
    @DisplayName("debit(BigDecimal) should decrease balance and normalize to 2 decimals")
    void debitDecreasesBalance() {
        Account a = new Account("D-1", "Dave", new BigDecimal("25.00")); // ACTIVE
        a.debit(new BigDecimal("5.499")); // normalize to 5.50
        assertEquals(0, a.getBalance().compareTo(new BigDecimal("19.50")));
    }

    @Test
    @DisplayName("debit(BigDecimal) should throw for insufficient funds")
    void debitInsufficientFunds() {
        Account a = new Account("E-1", "Eve", new BigDecimal("10.00")); // ACTIVE
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> a.debit(new BigDecimal("10.01")));
        assertTrue(ex.getMessage().contains("Insufficient balance"));
    }

    @Test
    @DisplayName("credit/debit require ACTIVE status")
    void operationsRequireActive() {
        Account locked = new Account("L-1", "Leo", new BigDecimal("10.00"), AccountStatus.LOCKED);
        assertThrows(IllegalStateException.class, () -> locked.credit(new BigDecimal("1.00")));
        assertThrows(IllegalStateException.class, () -> locked.debit(new BigDecimal("1.00")));

        Account closed = new Account("X-1", "Xena", new BigDecimal("10.00"), AccountStatus.CLOSED);
        assertThrows(IllegalStateException.class, () -> closed.credit(new BigDecimal("1.00")));
        assertThrows(IllegalStateException.class, () -> closed.debit(new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("setStatus should update status and bump version/timestamp")
    void setStatusBumpsVersionAndTimestamp() {
        Account a = new Account("S-1", "Sam", new BigDecimal("10.00"));
        long v1 = a.getVersion();
        var t1 = a.getLastUpdated();

        a.setStatus(AccountStatus.LOCKED);

        assertEquals(AccountStatus.LOCKED, a.getStatus());
        assertTrue(a.getVersion() > v1, "Version should increment");
        assertTrue(a.getLastUpdated().isAfter(t1), "Timestamp should update");
    }

    @Test
    @DisplayName("Helper from TestDataFactory should produce ACTIVE account with String id and normalized balance")
    void factoryActiveAccount() {
        // Uses your TestDataFactory which converts Long -> String and passes BigDecimal
        Account a = TestDataFactory.activeAccount(100L, new BigDecimal("30.1"));

        assertEquals("100", a.getId());
        assertEquals("ACC-100", a.getHolderName());
        assertEquals(AccountStatus.ACTIVE, a.getStatus());
        assertEquals(0, a.getBalance().compareTo(new BigDecimal("30.10")));
    }

    @Test
    @DisplayName("credit/debit should reject non-positive amounts")
    void nonPositiveAmountsRejected() {
        Account a = new Account("N-1", "Nina", new BigDecimal("10.00"));
        assertThrows(IllegalArgumentException.class, () -> a.credit(null));
        assertThrows(IllegalArgumentException.class, () -> a.debit(null));
        assertThrows(IllegalArgumentException.class, () -> a.credit(new BigDecimal("0.00")));
        assertThrows(IllegalArgumentException.class, () -> a.debit(new BigDecimal("0.00")));
        assertThrows(IllegalArgumentException.class, () -> a.credit(new BigDecimal("-1.00")));
        assertThrows(IllegalArgumentException.class, () -> a.debit(new BigDecimal("-1.00")));
    }
}