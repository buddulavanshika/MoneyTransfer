package com.mts.domain.model;

import com.mts.domain.enums.AccountStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a bank account.
 * <p>
 * Responsibilities:
 * - Hold core state (id, holderName, balance, status, version, lastUpdated)
 * - Enforce invariants around credit/debit operations
 * - Expose status checks (isActive)
 *
 * Notes:
 * - Monetary operations are done with BigDecimal and normalized to scale 2 (HALF_UP).
 * - debit/credit are synchronized to keep updates atomic for this entity instance.
 */
public class Account {

    private final String id;
    private final String holderName;

    // Monetary values kept at scale=2 for cents/paise-style precision
    private BigDecimal balance;

    private AccountStatus status;

    // Simple optimistic versioning for domain changes
    private long version;

    private Instant lastUpdated;

    /**
     * Constructs an Account with explicit initial status.
     *
     * @param id              unique identifier (e.g., UUID as string)
     * @param holderName      non-blank account holder name
     * @param openingBalance  non-negative initial balance, scale normalized to 2
     * @param status          initial status (e.g., ACTIVE, LOCKED, CLOSED)
     */
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

    /**
     * Convenience constructor that defaults status to ACTIVE.
     */
    public Account(String id, String holderName, BigDecimal openingBalance) {
        this(id, holderName, openingBalance, AccountStatus.ACTIVE);
    }

    /**
     * Credits the account by the given amount.
     * <ul>
     *   <li>Requires account to be ACTIVE.</li>
     *   <li>Amount must be &gt; 0 (strictly positive).</li>
     *   <li>Balance is normalized to scale=2.</li>
     * </ul>
     *
     * @param amount strictly positive amount
     * @throws IllegalStateException    if account is not ACTIVE
     * @throws IllegalArgumentException if amount is null or not positive
     */
    public synchronized void credit(BigDecimal amount) {
        ensureActive();
        BigDecimal normalized = normalizePositive(amount, "amount");
        this.balance = this.balance.add(normalized).setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    /**
     * Debits the account by the given amount.
     * <ul>
     *   <li>Requires account to be ACTIVE.</li>
     *   <li>Amount must be &gt; 0 (strictly positive).</li>
     *   <li>Resulting balance must not go negative.</li>
     * </ul>
     *
     * @param amount strictly positive amount
     * @throws IllegalStateException    if account is not ACTIVE or insufficient funds
     * @throws IllegalArgumentException if amount is null or not positive
     */
    public synchronized void debit(BigDecimal amount) {
        ensureActive();
        BigDecimal normalized = normalizePositive(amount, "amount");
        if (this.balance.compareTo(normalized) < 0) {
            throw new IllegalStateException("Insufficient balance: attempted " + normalized + ", available " + this.balance);
        }
        this.balance = this.balance.subtract(normalized).setScale(2, RoundingMode.HALF_UP);
        touch();
    }

    /**
     * @return true if the account status is ACTIVE.
     */
    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    // ---------- Helpers & Invariants ----------

    private static String requireNonBlank(String value, String field) {
        Objects.requireNonNull(value, field + " cannot be null");
        if (value.isBlank()) throw new IllegalArgumentException(field + " cannot be blank");
        return value;
    }

    private static BigDecimal normalizeNonNegative(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() < 0) {
            throw new IllegalArgumentException(field + " must be >= 0.00");
        }
        return scaled;
    }

    private static BigDecimal normalizePositive(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        if (scaled.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be > 0.00");
        }
        return scaled;
    }

    private void ensureActive() {
        if (!isActive()) {
            throw new IllegalStateException("Account " + id + " is not ACTIVE (status=" + status + ")");
        }
    }

    private void touch() {
        this.version++;
        this.lastUpdated = Instant.now();
    }

    // ---------- Getters / Mutators (status only) ----------

    public String getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    /**
     * Domain-level status change (e.g., LOCK, CLOSE).
     * Updating status increments version and timestamp.
     */
    public void setStatus(AccountStatus status) {
        this.status = Objects.requireNonNull(status, "status");
        touch();
    }

    public long getVersion() {
        return version;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    // Optional: fluent helpers for readability
    public boolean isLocked() {
        return this.status == AccountStatus.LOCKED;
    }

    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", holderName='" + holderName + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", version=" + version +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}