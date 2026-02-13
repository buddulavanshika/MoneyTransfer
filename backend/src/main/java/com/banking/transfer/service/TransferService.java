package com.banking.transfer.service;

import com.banking.transfer.dto.TransactionResponse;
import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.entity.Account;
import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.exception.AccountNotActiveException;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.DuplicateTransferException;
import com.banking.transfer.exception.InsufficientBalanceException;
import com.banking.transfer.repository.AccountRepository;
import com.banking.transfer.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        log.info("Processing transfer from {} to {} for amount {}",
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount());

        validateTransferRequest(request);

        // 🔥 Idempotency check
        if (transactionLogRepository
                .findByIdempotencyKey(request.getIdempotencyKey())
                .isPresent()) {

            throw new DuplicateTransferException(
                    "Duplicate transfer request with idempotency key: "
                            + request.getIdempotencyKey());
        }

        try {

            Account fromAccount = accountRepository
                    .findById(request.getFromAccountId())
                    .orElseThrow(() ->
                            new AccountNotFoundException(
                                    "Source account not found: "
                                            + request.getFromAccountId()));

            Account toAccount = accountRepository
                    .findById(request.getToAccountId())
                    .orElseThrow(() ->
                            new AccountNotFoundException(
                                    "Destination account not found: "
                                            + request.getToAccountId()));

            // Validate account status
            if (!fromAccount.isActive()) {
                throw new AccountNotActiveException("Source account is not active");
            }

            if (!toAccount.isActive()) {
                throw new AccountNotActiveException("Destination account is not active");
            }

            // Validate balance
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance in source account");
            }

            // Debit → Credit
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            TransactionLog successLog = TransactionLog.builder()
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey(request.getIdempotencyKey())
                    .build();

            TransactionLog savedLog = transactionLogRepository.save(successLog);

            log.info("Transfer completed successfully. Transaction ID: {}",
                    savedLog.getId());

            return TransferResponse.builder()
                    .transactionId(savedLog.getId())
                    .status("SUCCESS")
                    .message("Transfer completed successfully")
                    .debitedFrom(request.getFromAccountId())
                    .creditedTo(request.getToAccountId())
                    .amount(request.getAmount())
                    .build();

        } catch (Exception e) {

            TransactionLog failedLog = TransactionLog.builder()
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .status(TransactionStatus.FAILED)
                    .failureReason(e.getMessage())
                    .idempotencyKey(request.getIdempotencyKey())
                    .build();

            transactionLogRepository.save(failedLog);

            log.error("Transfer failed: {}", e.getMessage());
            throw e;
        }
    }

    private void validateTransferRequest(TransferRequest request) {

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }

    // ✅ Transaction history with FAILED + reason included
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long accountId) {

        List<TransactionLog> logs =
                transactionLogRepository.findByAccountId(accountId);

        return logs.stream().map(log -> {

            String type =
                    accountId.equals(log.getFromAccountId())
                            ? "DEBIT"
                            : "CREDIT";

            return TransactionResponse.builder()
                    .id(log.getId())
                    .fromAccountId(log.getFromAccountId())
                    .toAccountId(log.getToAccountId())
                    .amount(log.getAmount())
                    .status(log.getStatus())
                    .failureReason(log.getFailureReason()) // ✅ failure reason visible
                    .createdOn(log.getCreatedOn())
                    .type(type)
                    .build();

        }).toList();
    }
}
