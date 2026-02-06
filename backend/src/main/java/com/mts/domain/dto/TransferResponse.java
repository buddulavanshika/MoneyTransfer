package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import com.mts.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor // Required for JSON serialization
@AllArgsConstructor // Standard constructor for all 9 fields
public class TransferResponse {
    private String transactionId;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String message;
    private String idempotencyKey;
    private Instant createdOn;

    // Custom constructor for simple messages as you requested
    public TransferResponse(String transactionId, String message) {
        this.transactionId = transactionId;
        this.message = message;
        this.status = TransactionStatus.SUCCESS;
        this.createdOn = Instant.now();
    }
}