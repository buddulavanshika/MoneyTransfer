// com.mts.application.mappers.TransactionMapper
package com.mts.application.mapper;
import com.mts.application.entities.TransactionLog;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static TransactionLog toDTO(TransactionLog l) {
        TransactionLog dto = new TransactionLog();
        dto.setId(l.getId());
        dto.setIdempotencyKey(l.getIdempotencyKey());
        dto.setFromAccountId(l.getFromAccountId());
        dto.setToAccountId(l.getToAccountId());
        dto.setAmount(l.getAmount());
        dto.setCurrency(l.getCurrency());
        dto.setStatus(l.getStatus());
        dto.setFailureReason(l.getFailureReason());
        dto.setCreatedOn(l.getCreatedOn());
        return dto;
    }
}