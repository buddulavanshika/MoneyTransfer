package com.mts.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Input DTO for initiating a money transfer.
 *
 * This DTO is validated via Jakarta Bean Validation.
 * In Module 2, you can validate it programmatically in tests using Hibernate Validator.
 * In Module 3, Spring Boot will validate it automatically with @Valid in your controllers.
 */
public class TransferRequest {

    /**
     * Source (debit) account identifier.
     * Kept as @NotNull per your validation requirement. If you want stricter rules in the future,
     * you can change to @NotBlank and add a @Pattern for UUID/format.
     */
    @NotNull
    private String sourceAccountId;

    /**
     * Destination (credit) account identifier.
     */
    @NotNull
    private String destinationAccountId;

    /**
     * Transfer amount. Must be >= 0.01.
     * Prefer BigDecimal for monetary values.
     */
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    /**
     * ISO 4217 currency code (e.g., "INR", "USD").
     * Using @NotNull as per your requirement; consider @NotBlank for stricter non-empty checks later.
     */
    @NotNull
    private String currency;

    /**
     * Idempotency key used to avoid duplicate transfers on retried requests.
     * Not strictly required for validation here, but highly recommended operationally.
     */
    @NotNull
    private String idempotencyKey;

    // ---- Constructors ----

    public TransferRequest() {
        // no-arg constructor for frameworks and tests
    }

    public TransferRequest(String sourceAccountId,
                           String destinationAccountId,
                           BigDecimal amount,
                           String currency,
                           String idempotencyKey) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
    }

    // ---- Getters & Setters ----

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "sourceAccountId='" + sourceAccountId + '\'' +
                ", destinationAccountId='" + destinationAccountId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                '}';
    }
}