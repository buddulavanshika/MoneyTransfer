package com.mts.domain.model;

import com.mts.domain.enums.AccountStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a bank account.
 */
public class Account {

    private final String id;
    private final String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private long version;
    private Instant lastUpdated;

    public Account(String id,
                   String holderName,
                   BigDecimal openingBalance,
                   AccountStatus status) {

        this.id = requireNonBlank(id, "id");
        this.holderName = requireNonBlank(holderName, "holderName");
        this.balance = normalizeNonNegative(openingBalance, "openingBalance");
        this.status = Objects.requireNonNull(status, "status");
        this.version = 0L;
        this.lastUpdated = Instant.now();
    }

    public Account(String id, String holderName, BigDecimal openingBalance) {
        this(id, holderName, openingBalance, AccountStatus.ACTIVE);
    }

    public synchronized void credit(BigDecimal amount) {
        ensureActive();
        BigDecimal normalized = normalizePositive(amount, "amount");
        this.balance = this.balance.add(normalized).setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    public synchronized void debit(BigDecimal amount) {
        ensureActive();
        BigDecimal normalized = normalizePositive(amount, "amount");

        if (this.balance.compareTo(normalized) < 0) {
            throw new IllegalStateException(
                "Insufficient balance: attempted " + normalized + ", available " + balance
            );
        }

        this.balance = this.balance.subtract(normalized).setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    // ---------- Helpers ----------

    private static String requireNonBlank(String value, String field) {
        Objects.requireNonNull(value, field + " cannot be null");
        if (value.isBlank()) throw new IllegalArgumentException(field + " cannot be blank");
        return value;
    }

    private static BigDecimal normalizeNonNegative(BigDecimal value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " cannot be null");
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() < 0) throw new IllegalArgumentException(field + " must be >= 0.00");
        return scaled;
    }

    private static BigDecimal normalizePositive(BigDecimal value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " cannot be null");
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() <= 0) throw new IllegalArgumentException(field + " must be > 0.00");
        return scaled;
    }

    private void ensureActive() {
        if (!isActive()) {
            throw new IllegalStateException(
                "Account " + id + " is not ACTIVE (status=" + status + ")"
            );
        }
    }

    private void touch() {
        version++;
        lastUpdated = Instant.now();
    }

    // ---------- Getters ----------

    public String getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    /** Defensive copy */
    public BigDecimal getBalance() {
        return balance.setScale(2, RoundingMode.HALF_UP);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    // ---------- Mutators ----------

    public void setStatus(AccountStatus status) {
        Objects.requireNonNull(status, "status");
        if (this.status != status) {
            this.status = status;
            touch();
        }
    }

    public boolean isLocked() {
        return status == AccountStatus.LOCKED;
    }

    public boolean isClosed() {
        return status == AccountStatus.CLOSED;
    }
}
