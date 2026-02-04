package com.mts.domain.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private static final Currency INR = Currency.getInstance("INR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void factoryNormalizesScaleHalfEven() {
        Money m = Money.of(new BigDecimal("10.235"), INR); // HALF_EVEN => 10.24
        assertEquals(new BigDecimal("10.24"), m.getAmount());
        assertEquals(INR, m.getCurrency());
    }

    @Test
    void addAndSubtractRequireSameCurrency() {
        Money a = Money.of(new BigDecimal("5.00"), INR);
        Money b = Money.of(new BigDecimal("2.50"), INR);

        Money sum = a.add(b);
        assertEquals(new BigDecimal("7.50"), sum.getAmount());
        assertEquals(INR, sum.getCurrency());

        Money diff = a.subtract(b);
        assertEquals(new BigDecimal("2.50"), diff.getAmount());
        assertEquals(INR, diff.getCurrency());
    }

    @Test
    void multiplyNormalizesViaConstructor() {
        Money a = Money.of(new BigDecimal("2.10"), INR);
        Money result = a.multiply(new BigDecimal("2.5")); // 5.25
        assertEquals(new BigDecimal("5.25"), result.getAmount());
        assertEquals(INR, result.getCurrency());
    }

    @Test
    void comparisonsAndSigns() {
        Money zero = Money.zero(INR);
        Money pos = Money.of(new BigDecimal("0.01"), INR);
        Money neg = Money.of(new BigDecimal("-0.01"), INR);

        assertTrue(zero.isZero());
        assertTrue(pos.isPositive());
        assertTrue(neg.isNegative());

        assertTrue(pos.compareTo(zero) > 0);
        assertTrue(neg.compareTo(zero) < 0);
    }

    @Test
    void currencyMismatchThrows() {
        Money inr = Money.of(new BigDecimal("1.00"), INR);
        Money usd = Money.of(new BigDecimal("1.00"), USD);

        assertThrows(IllegalArgumentException.class, () -> inr.add(usd));
        assertThrows(IllegalArgumentException.class, () -> inr.subtract(usd));
        assertThrows(IllegalArgumentException.class, () -> inr.compareTo(usd));
    }

    @Test
    void equalsHashCodeToString() {
        Money a = Money.of(new BigDecimal("1.00"), INR);
        Money b = Money.of(new BigDecimal("1.000"), INR); // normalized to 1.00

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().startsWith("INR "));
    }
}