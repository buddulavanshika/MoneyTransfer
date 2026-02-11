package com.mts.application.mapper;

import com.mts.application.entities.TransactionLog;
import com.mts.domain.dto.TransactionLogResponse;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static TransactionLogResponse toResponse(TransactionLog entity) {
        if (entity == null) return null;
        return new TransactionLogResponse(
                entity.getId(),
                entity.getFromAccountId(),
                entity.getToAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getIdempotencyKey(),
                entity.getCreatedOn()
        );
    }
}
