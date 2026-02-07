package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.entities.TransactionLog;
import com.mts.application.mapper.TransactionMapper;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.application.repository.spec.TransactionLogSpecs;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.DuplicateTransferException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final TransactionLogRepository logRepository;

    /**
     * Idempotent transfer with String account IDs.
     * Flow:
     * 1) Claim idempotency by inserting a PENDING log (unique constraint on idempotency_key).
     * 2) Validate request & accounts.
     * 3) Execute transfer (debit/credit).
     * 4) Mark SUCCESS (or FAILED on exception), then return response.
     */
    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Resolve String account IDs from the request
        final String fromId = resolveFromId(request);
        final String toId   = resolveToId(request);

        // 1) Claim idempotency (PENDING)
        TransactionLog log = new TransactionLog();
        log.setIdempotencyKey(request.getIdempotencyKey());
        log.setFromAccountId(Long.valueOf(fromId));          // String
        log.setToAccountId(Long.valueOf(toId));              // String
        log.setAmount(request.getAmount());
        log.setCurrency(request.getCurrency());
        log.setStatus(TransactionStatus.PENDING);
        log.setFailureReason(null);
        log.setCreatedOn(OffsetDateTime.now()); // or OffsetDateTime.now(ZoneOffset.UTC)

        try {
            // Flush now so duplicate keys trigger DataIntegrityViolationException here
            log = logRepository.saveAndFlush(log);
        } catch (DataIntegrityViolationException dup) {
            // Option A: reject duplicates (current behavior)
            throw new DuplicateTransferException("Duplicate transaction: " + request.getIdempotencyKey());

            // Option B: return previous SUCCESS result (uncomment if desired)
            // var existing = logRepository.findByIdempotencyKey(request.getIdempotencyKey()).orElse(null);
            // if (existing != null && existing.getStatus() == TransactionStatus.SUCCESS) {
            //     return new TransferResponse(existing.getId().toString(), "Duplicate request: returning previous result");
            // }
            // throw new DuplicateTransferException("Duplicate transaction: " + request.getIdempotencyKey());
        }

        try {
            // 2) Validate request & accounts
            validateTransfer(fromId, toId, request.getAmount(), request.getCurrency());

            // 3) Load accounts (String IDs)
            Account sender = accountService.getAccountById(fromId);
            Account receiver = accountService.getAccountById(toId);

            // 4) Execute the transfer (throws on insufficient funds, etc.)
            executeTransfer(sender, receiver, request.getAmount());

            // 5) Mark SUCCESS
            log.setStatus(TransactionStatus.SUCCESS);
            log.setFailureReason(null);
            logRepository.save(log);

            return new TransferResponse(log.getId().toString(), "Transfer Completed Successfully");

        } catch (Exception e) {
            // 6) Mark FAILED for audit, then rethrow to rollback money movement
            log.setStatus(TransactionStatus.FAILED);
            log.setFailureReason(e.getMessage());
            logRepository.save(log);
            throw e;
        }
    }

    /**
     * Validates input and account state (existence + ACTIVE) for String IDs.
     * Adds basic business rules: non-null/positive amount, non-blank currency, different accounts.
     */
    private void validateTransfer(String fromId, String toId, BigDecimal amount, String currency) {
        if (fromId == null || fromId.isBlank()) {
            throw new IllegalArgumentException("Missing source account id");
        }
        if (toId == null || toId.isBlank()) {
            throw new IllegalArgumentException("Missing destination account id");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        // Delegate domain validations (exists + ACTIVE, etc.)
        accountService.validateAccountForTransfer(fromId);
        accountService.validateAccountForTransfer(toId);
    }

    /**
     * Performs the actual debit/credit.
     * Assumes Account.debit(...) enforces insufficient funds rule,
     * and Account.credit(...) increments the balance.
     */
    private void executeTransfer(Account sender, Account receiver, BigDecimal amount) {
        sender.debit(amount);
        receiver.credit(amount);
    }

    // ===== Helpers (String-first) =====

    /**
     * Prefer a String 'fromAccountId' in the request.
     * If your DTO has both 'fromAccountId' and 'sourceAccountId', use whichever is non-blank.
     */
    private String resolveFromId(TransferRequest req) {
        String id = null;
        try {
            // If your DTO has getFromAccountId() as String, use it:
            id = safeTrim(String.valueOf(req.getFromAccountId()));
        } catch (NoSuchMethodError | Exception ignore) {
            // ignore if DTO doesn’t have this accessor
        }
        if (isBlank(id)) {
            id = safeTrim(req.getSourceAccountId()); // fallback
        }
        if (isBlank(id)) {
            throw new IllegalArgumentException("Missing fromAccountId/sourceAccountId");
        }
        return id;
    }

    /**
     * Prefer a String 'toAccountId' in the request.
     * If your DTO has both 'toAccountId' and 'destinationAccountId', use whichever is non-blank.
     */
    private String resolveToId(TransferRequest req) {
        String id = null;
        try {
            // If your DTO has getToAccountId() as String, use it:
            id = safeTrim(String.valueOf(req.getToAccountId()));
        } catch (NoSuchMethodError | Exception ignore) {
        }
        if (isBlank(id)) {
            id = safeTrim(req.getDestinationAccountId()); // fallback
        }
        if (isBlank(id)) {
            throw new IllegalArgumentException("Missing toAccountId/destinationAccountId");
        }
        return id;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    // =========================
    // Transaction history (Page)
    // =========================

    @Override
    public Page<TransactionLog> getAccountTransactions(
            String accountId,
            OffsetDateTime from,
            OffsetDateTime to,
            TransactionStatus status,
            TransferService.Direction direction,
            Pageable pageable
    ) {
        // Base spec: involve the account either as sender or receiver
        Specification<TransactionLog> spec = where(TransactionLogSpecs.forAccount(accountId))
                .and(TransactionLogSpecs.createdOnFrom(from))
                .and(TransactionLogSpecs.createdOnTo(to))
                .and(TransactionLogSpecs.status(status));

        // Direction filter if requested
        if (direction == TransferService.Direction.SENT) {
            spec = where(TransactionLogSpecs.directionSentOnly(accountId))
                    .and(TransactionLogSpecs.createdOnFrom(from))
                    .and(TransactionLogSpecs.createdOnTo(to))
                    .and(TransactionLogSpecs.status(status));
        } else if (direction == TransferService.Direction.RECEIVED) {
            spec = where(TransactionLogSpecs.directionReceivedOnly(accountId))
                    .and(TransactionLogSpecs.createdOnFrom(from))
                    .and(TransactionLogSpecs.createdOnTo(to))
                    .and(TransactionLogSpecs.status(status));
        }

        return logRepository.findAll(spec, pageable)
                .map(TransactionMapper::toDTO);
    }
}