package com.mts.domain.util;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable Value Object representing monetary value.
 *
 * Design Principles:
 * - Immutable and thread-safe
 * - Currency-aware
 * - Fixed scale (2) with banker’s rounding (HALF_EVEN)
 * - No floating-point arithmetic
 *
 * Used in:
 * - Account debit / credit logic
 * - MoneyTransferDomainService
 */
public final class Money implements Comparable<Money>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = normalize(amount);
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
    }

    /* ========= Factory Methods ========= */

    public static Money of(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        return new Money(amount, currency);
    }

    public static Money of(double amount, Currency currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }

    /* ========= Getters ========= */

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    /* ========= Arithmetic Operations ========= */

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "Multiplier must not be null");
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    /* ========= Comparisons ========= */

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    /* ========= Helpers ========= */

    private static BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Money must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency
            );
        }
    }

    /* ========= Object Overrides ========= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
