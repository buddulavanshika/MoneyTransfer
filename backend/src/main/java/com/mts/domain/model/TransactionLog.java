package com.mts.domain.model;

import com.mts.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain record representing the result of a transfer.
 *
 * This class is a simple data container with no business methods,
 * per Module 2 requirements.
 */
public class TransactionLog {

    private String id;
    private String fromAccountId;
    private String toAccountId;

    private BigDecimal amount;

    private TransactionStatus status;
    private String failureReason;

    private String idempotencyKey;
    private Instant createdOn;

    // ----- Constructors -----

    public TransactionLog() {
        // No-arg constructor
    }

    public TransactionLog(String id,
                          String fromAccountId,
                          String toAccountId,
                          BigDecimal amount,
                          TransactionStatus status,
                          String failureReason,
                          String idempotencyKey,
                          Instant createdOn) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.idempotencyKey = idempotencyKey;
        this.createdOn = createdOn;
    }

    // ----- Getters & Setters -----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }
}