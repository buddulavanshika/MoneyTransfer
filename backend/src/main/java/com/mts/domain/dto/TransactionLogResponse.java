package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for transaction log entries in API responses (e.g. transaction history).
 */
public record TransactionLogResponse(
        String id,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String failureReason,
        String idempotencyKey,
        Instant createdOn
) {}
