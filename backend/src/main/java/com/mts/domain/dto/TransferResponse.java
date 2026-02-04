package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Output DTO for transfer API.
 *
 * Immutable and concise using Java 17 record.
 * Suitable for returning from controllers in Module 3.
 */
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
) { }