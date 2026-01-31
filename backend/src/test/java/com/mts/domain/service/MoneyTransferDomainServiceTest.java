package com.mts.domain.service;

import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.model.Account;
import com.mts.domain.model.TransactionLog;
import com.mts.domain.util.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.mts.support.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTransferDomainServiceTest {

    private MoneyTransferDomainService service;

    @BeforeEach
    void setUp() {
        service = new MoneyTransferDomainService();
        service.resetIdempotencyKeys(); // ensure clean state per test
    }

    @Test
    @DisplayName("Happy path: debit then credit, returns SUCCESS TransactionLog")
    void testTransfer_HappyPath() {
        Account from = activeAccount(1L, new BigDecimal("1000.00"));
        Account to   = activeAccount(2L, new BigDecimal("400.00"));
        Money amount = money(new BigDecimal("200.00"));

        TransactionLog log = service.transfer(from, to, amount, "IDEMP-001");

        assertNotNull(log.getId());
        assertEquals(from.getId(), log.getFromAccountId());
        assertEquals(to.getId(), log.getToAccountId());
        assertEquals(0, amount.getAmount().compareTo(log.getAmount()));
        assertEquals(TransactionStatus.SUCCESS, log.getStatus());
        assertEquals("IDEMP-001", log.getIdempotencyKey());
        assertNotNull(log.getCreatedOn());
        assertNull(log.getFailureReason());

        // balances updated
        assertEquals(0, new BigDecimal("800.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("600.00").compareTo(to.getBalance()));
    }

    @Test
    @DisplayName("Same account transfer should fail with IllegalArgumentException")
    void testTransfer_SameAccount() {
        Account acc = activeAccount(1L, new BigDecimal("1000.00"));
        Money amount = money(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(acc, acc, amount, "IDEMP-002"));
    }

    @Test
    @DisplayName("Inactive source account should fail")
    void testTransfer_InactiveSource() {
        Account from = lockedAccount(1L, new BigDecimal("1000.00"));
        Account to   = activeAccount(2L, new BigDecimal("400.00"));

        assertThrows(AccountNotActiveException.class,
                () -> service.transfer(from, to, money(new BigDecimal("100.00")), "IDEMP-003"));
    }

    @Test
    @DisplayName("Inactive destination account should fail")
    void testTransfer_InactiveDestination() {
        Account from = activeAccount(1L, new BigDecimal("1000.00"));
        Account to   = closedAccount(2L, new BigDecimal("400.00"));

        assertThrows(AccountNotActiveException.class,
                () -> service.transfer(from, to, money(new BigDecimal("100.00")), "IDEMP-004"));
    }

    @Test
    @DisplayName("Null accounts should map to AccountNotFoundException")
    void testTransfer_NullAccounts() {
        // First case: null source
        final Account to1 = activeAccount(2L, new BigDecimal("400.00"));
        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(null, to1, money(new BigDecimal("50.00")), "IDEMP-005"));

        // Second case: null destination
        final Account from2 = activeAccount(1L, new BigDecimal("1000.00"));
        assertThrows(AccountNotFoundException.class,
                () -> service.transfer(from2, null, money(new BigDecimal("50.00")), "IDEMP-006"));
    }

    @Test
    @DisplayName("Zero or negative amounts should fail validation")
    void testTransfer_InvalidAmount() {
        final Account from = activeAccount(1L, new BigDecimal("1000.00"));
        final Account to   = activeAccount(2L, new BigDecimal("400.00"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, to, money(new BigDecimal("0.00")), "IDEMP-007"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, to, money(new BigDecimal("-10.00")), "IDEMP-008"));
    }

    @Test
    @DisplayName("Insufficient balance should throw InsufficientBalanceException and not credit receiver")
    void testTransfer_InsufficientFunds() {
        final Account from = activeAccount(1L, new BigDecimal("100.00"));
        final Account to   = activeAccount(2L, new BigDecimal("400.00"));

        assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(from, to, money(new BigDecimal("150.00")), "IDEMP-009"));

        // Balances unchanged
        assertEquals(0, new BigDecimal("100.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("400.00").compareTo(to.getBalance()));
    }

    @Test
    @DisplayName("Idempotency: second call with same key should fail with DuplicateTransferException")
    void testTransfer_DuplicateIdempotencyKey() {
        final Account from = activeAccount(1L, new BigDecimal("1000.00"));
        final Account to   = activeAccount(2L, new BigDecimal("400.00"));

        service.transfer(from, to, money(new BigDecimal("50.00")), "IDEMP-010");

        assertThrows(DuplicateTransferException.class,
                () -> service.transfer(from, to, money(new BigDecimal("50.00")), "IDEMP-010"));
    }

    @Test
    @DisplayName("Overload without idempotency key should still succeed")
    void testTransfer_AutoIdempotencyOverload() {
        final Account from = activeAccount(1L, new BigDecimal("1000.00"));
        final Account to   = activeAccount(2L, new BigDecimal("400.00"));

        TransactionLog log = service.transfer(from, to, money(new BigDecimal("75.00")));

        assertNotNull(log);
        assertEquals(TransactionStatus.SUCCESS, log.getStatus());
        assertNotNull(log.getIdempotencyKey()); // auto-generated
        assertTrue(log.getIdempotencyKey().startsWith("AUTO-"));
    }
}