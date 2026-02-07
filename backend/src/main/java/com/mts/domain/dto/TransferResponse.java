package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transactionId,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String message,
        String idempotencyKey,
        Instant createdOn
) {
    // Custom constructor for simple messages
    public TransferResponse(String transactionId, String message) {
        this(
                transactionId,
                null,                 // sourceAccountId
                null,                 // destinationAccountId
                null,                 // amount
                null,                 // currency
                TransactionStatus.SUCCESS,
                message,
                null,                 // idempotencyKey
                Instant.now()         // createdOn
        );
    }
}
