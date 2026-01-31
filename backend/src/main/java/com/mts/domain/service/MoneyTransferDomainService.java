package com.mts.domain.service;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.model.Account;
import com.mts.domain.model.TransactionLog;
import com.mts.domain.util.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pure domain service that orchestrates money transfer using domain rules.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate inputs (accounts, status, amount)</li>
 *   <li>Enforce idempotency (in-memory for Module 2)</li>
 *   <li>Execute business sequence: debit (source) → credit (destination)</li>
 *   <li>Return a SUCCESS {@link TransactionLog} on completion</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>Failures are represented via domain exceptions; this service does not create failure logs.</li>
 *   <li>In Module 3, idempotency should be persisted (e.g., DB unique constraint) and the service
 *       will be wrapped in a transactional application/service layer.</li>
 * </ul>
 */
public class MoneyTransferDomainService {

    /**
     * In-memory idempotency store for Module 2 (test-friendly).
     * Replaced by persistent uniqueness in Module 3.
     */
    private final Set<String> usedIdempotencyKeys = ConcurrentHashMap.newKeySet();

    /**
     * Execute a transfer with an explicit idempotency key.
     *
     * @param from           Source account (must be ACTIVE)
     * @param to             Destination account (must be ACTIVE)
     * @param amount         Money to transfer (must be > 0)
     * @param idempotencyKey Unique key per request; must not repeat
     * @return Success {@link TransactionLog}
     *
     * @throws AccountNotFoundException     if any account is null
     * @throws AccountNotActiveException    if any account is not ACTIVE
     * @throws IllegalArgumentException     if accounts are same or amount invalid or key blank
     * @throws DuplicateTransferException   if idempotency key was already used
     * @throws InsufficientBalanceException if source balance is insufficient (thrown by debit)
     */
    public TransactionLog transfer(Account from, Account to, Money amount, String idempotencyKey) {
        validateInputs(from, to, amount, idempotencyKey);
        enforceIdempotency(idempotencyKey);

        // Business sequence: debit first (may throw), then credit
        // Expecting Account.debit/credit to accept BigDecimal:
        from.debit(amount.getAmount());
        to.credit(amount.getAmount());

        // Build success transaction log
        TransactionLog log = new TransactionLog();
        log.setId(UUID.randomUUID().toString());   // setId(String)
        log.setFromAccountId(from.getId());
        log.setToAccountId(to.getId());
        log.setAmount(amount.getAmount());         // setAmount(BigDecimal)
        log.setStatus(TransactionStatus.SUCCESS);
        log.setFailureReason(null);
        log.setIdempotencyKey(idempotencyKey);
        log.setCreatedOn(Instant.now());           // setCreatedOn(Instant)

        return log;
    }

    /**
     * Execute a transfer using an auto-generated idempotency key.
     * Useful for internal flows or tests where explicit keying isn't required.
     */
    public TransactionLog transfer(Account from, Account to, Money amount) {
        String autoKey = "AUTO-" + UUID.randomUUID();
        return transfer(from, to, amount, autoKey);
    }

    /* =========================
       Validation & Idempotency
       ========================= */

    private void validateInputs(Account from, Account to, Money amount, String idempotencyKey) {
        if (from == null) {
            throw new AccountNotFoundException("Source account not found");
        }
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found");
        }
        if (Objects.equals(from.getId(), to.getId())) {
            throw new IllegalArgumentException("Accounts must be different");
        }
        ensureActive(from);
        ensureActive(to);

        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must be provided");
        }

        // If your Account tracks currency, enforce matching currency here.
        // Example:
        // if (!Objects.equals(from.getCurrency(), amount.getCurrency()) ||
        //     !Objects.equals(to.getCurrency(), amount.getCurrency())) { ... }
    }

    private void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + account.getId() + " is not ACTIVE");
        }
    }

    private void enforceIdempotency(String idempotencyKey) {
        boolean firstUse = usedIdempotencyKeys.add(idempotencyKey);
        if (!firstUse) {
            throw new DuplicateTransferException("Duplicate transfer request (idempotency)");
        }
    }

    /**
     * Test support: clears in-memory idempotency keys between tests.
     * Not intended for production usage.
     */
    public void resetIdempotencyKeys() {
        usedIdempotencyKeys.clear();
    }
}