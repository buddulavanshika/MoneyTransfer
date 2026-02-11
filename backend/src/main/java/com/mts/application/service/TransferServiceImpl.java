package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.entities.TransactionLog;
import com.mts.application.mapper.TransactionMapper;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.application.repository.spec.TransactionLogSpecs;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.dto.TransactionLogResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionLogRepository logRepository;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request)
            throws AccountNotFoundException,
            AccountNotActiveException,
            InsufficientBalanceException,
            DuplicateTransferException,
            OptimisticLockException {

        String fromIdStr = resolveFromId(request);
        String toIdStr = resolveToId(request);
        Long fromId = parseAccountId(fromIdStr, "source");
        Long toId = parseAccountId(toIdStr, "destination");

        // 1) Idempotency
        if (logRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()) {
            throw new DuplicateTransferException(
                    "Duplicate transfer request: " + request.getIdempotencyKey() + " (idempotency key already used)");
        }

        // 2) Create and persist PENDING log
        TransactionLog log = new TransactionLog();
        log.setId(UUID.randomUUID().toString());
        log.setIdempotencyKey(request.getIdempotencyKey());
        log.setFromAccountId(fromId);
        log.setToAccountId(toId);
        log.setAmount(request.getAmount());
        log.setCurrency(request.getCurrency());
        log.setStatus(TransactionStatus.PENDING);
        log.setFailureReason(null);
        log.setCreatedOn(Instant.now());

        try {
            log = logRepository.saveAndFlush(log);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateTransferException("Duplicate transfer request (idempotency key already used)", e);
        }

        try {
            // 3) Validate and load accounts
            validateTransfer(fromIdStr, toIdStr, request.getAmount());
            Account sender = accountService.getAccountById(fromIdStr);
            Account receiver = accountService.getAccountById(toIdStr);

            // 4) Debit and credit
            sender.debit(request.getAmount());
            receiver.credit(request.getAmount());

            // 5) Persist updated accounts
            accountRepository.saveAndFlush(sender);
            accountRepository.saveAndFlush(receiver);

            // 6) Mark SUCCESS
            log.setStatus(TransactionStatus.SUCCESS);
            logRepository.save(log);

            return buildSuccessResponse(log, fromIdStr, toIdStr, request);

        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.setStatus(TransactionStatus.FAILED);
            log.setFailureReason("Concurrent modification conflict");
            logRepository.save(log);
            throw new OptimisticLockException("Concurrent modification detected, please retry the transfer", e);
        } catch (InsufficientBalanceException | AccountNotActiveException | AccountNotFoundException
                e) {
            log.setStatus(TransactionStatus.FAILED);
            log.setFailureReason(e.getMessage());
            logRepository.save(log);
            throw e;
        }
    }

    private void validateTransfer(String fromId, String toId, BigDecimal amount)
            throws AccountNotFoundException, AccountNotActiveException {
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
        accountService.validateAccountForTransfer(fromId);
        accountService.validateAccountForTransfer(toId);
    }

    private TransferResponse buildSuccessResponse(TransactionLog log, String sourceId, String destId,
                                                  TransferRequest request) {
        return new TransferResponse(
                log.getId(),
                sourceId,
                destId,
                log.getAmount(),
                request.getCurrency(),
                TransactionStatus.SUCCESS,
                "Transfer completed successfully",
                log.getIdempotencyKey(),
                log.getCreatedOn());
    }

    private static Long parseAccountId(String idStr, String label) {
        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Missing " + label + " account id");
        }
        try {
            return Long.valueOf(idStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + label + " account id: " + idStr);
        }
    }

    private String resolveFromId(TransferRequest req) {
        String id = req.getSourceAccountId();
        if (id == null || id.isBlank()) {
            Long from = req.getFromAccountId();
            id = from != null ? String.valueOf(from) : null;
        }
        return id != null ? id.trim() : null;
    }

    private String resolveToId(TransferRequest req) {
        String id = req.getDestinationAccountId();
        if (id == null || id.isBlank()) {
            Long to = req.getToAccountId();
            id = to != null ? String.valueOf(to) : null;
        }
        return id != null ? id.trim() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionLogResponse> getAccountTransactions(
            String accountId,
            Instant from,
            Instant to,
            TransactionStatus status,
            Direction direction,
            Pageable pageable) {
        Long accountIdLong = parseAccountId(accountId, "account");
        Specification<TransactionLog> spec = where(TransactionLogSpecs.forAccount(accountIdLong))
                .and(TransactionLogSpecs.createdOnFrom(from))
                .and(TransactionLogSpecs.createdOnTo(to))
                .and(TransactionLogSpecs.status(status));

        if (direction == Direction.SENT) {
            spec = where(TransactionLogSpecs.directionSentOnly(accountIdLong))
                    .and(TransactionLogSpecs.createdOnFrom(from))
                    .and(TransactionLogSpecs.createdOnTo(to))
                    .and(TransactionLogSpecs.status(status));
        } else if (direction == Direction.RECEIVED) {
            spec = where(TransactionLogSpecs.directionReceivedOnly(accountIdLong))
                    .and(TransactionLogSpecs.createdOnFrom(from))
                    .and(TransactionLogSpecs.createdOnTo(to))
                    .and(TransactionLogSpecs.status(status));
        }

        return logRepository.findAll(spec, pageable).map(TransactionMapper::toResponse);
    }
}
