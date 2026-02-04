package com.mts.domain.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountResponseTest {

    @Test
    void noArgsConstructorShouldInitializeToNulls() {
        AccountResponse ar = new AccountResponse();

        assertNull(ar.getId());
        assertNull(ar.getHolderName());
        assertNull(ar.getBalance());
        assertNull(ar.getStatus());
    }

    @Test
    void allArgsConstructorShouldInitializeAllFields() {
        Long id = 100L;
        String holder = "Alice";
        BigDecimal balance = new BigDecimal("123.45");
        String status = "ACTIVE";

        AccountResponse ar = new AccountResponse(id, holder, balance, status);

        assertEquals(id, ar.getId());
        assertEquals(holder, ar.getHolderName());
        assertEquals(balance, ar.getBalance());
        assertEquals(status, ar.getStatus());
    }

    @Test
    void twoArgConstructorIdHolderShouldInitializeFields() {
        AccountResponse ar = new AccountResponse(200L, "Bob");

        assertEquals(200L, ar.getId());
        assertEquals("Bob", ar.getHolderName());
        assertNull(ar.getBalance());
        assertNull(ar.getStatus());
    }

    @Test
    void oneArgConstructorIdShouldInitializeField() {
        AccountResponse ar = new AccountResponse(300L);

        assertEquals(300L, ar.getId());
        assertNull(ar.getHolderName());
        assertNull(ar.getBalance());
        assertNull(ar.getStatus());
    }

    @Test
    void twoArgConstructorBalanceStatusShouldInitializeFields() {
        BigDecimal balance = new BigDecimal("999.99");
        AccountResponse ar = new AccountResponse(balance, "SUSPENDED");

        assertNull(ar.getId());
        assertNull(ar.getHolderName());
        assertEquals(balance, ar.getBalance());
        assertEquals("SUSPENDED", ar.getStatus());
    }

    @Test
    void settersShouldUpdateFields() {
        AccountResponse ar = new AccountResponse();

        ar.setId(10L);
        ar.setHolderName("Charlie");
        ar.setBalance(new BigDecimal("10.00"));
        ar.setStatus("ACTIVE");

        assertEquals(10L, ar.getId());
        assertEquals("Charlie", ar.getHolderName());
        assertEquals(new BigDecimal("10.00"), ar.getBalance());
        assertEquals("ACTIVE", ar.getStatus());
    }

    @Test
    void toStringShouldContainAllFields() {
        AccountResponse ar = new AccountResponse(1L, "Dana", new BigDecimal("1.23"), "CLOSED");

        String s = ar.toString();

        assertTrue(s.contains("AccountResponse"));
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("holderName='Dana'"));
        assertTrue(s.contains("balance=1.23"));
        assertTrue(s.contains("status='CLOSED'"));
    }

    // ------- equals/hashCode contract tests -------

    @Test
    void equalsShouldBeReflexive() {
        AccountResponse ar = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        assertEquals(ar, ar);
    }

    @Test
    void equalsShouldBeSymmetric() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equalsShouldBeTransitive() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse c = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    void equalsShouldBeConsistent() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        for (int i = 0; i < 5; i++) {
            assertEquals(a, b);
        }
    }

    @Test
    void equalsShouldHandleNullAndDifferentClass() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertNotEquals(a, null);
        assertNotEquals(a, "not-an-account");
    }

    @Test
    void hashCodeShouldBeConsistentWithEquals() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentObjectsShouldNotBeEqual() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(2L, "Frank", new BigDecimal("56.78"), "CLOSED");

        assertNotEquals(a, b);
    }

    @Test
    void equalityShouldConsiderAllFields() {
        AccountResponse baseline = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        AccountResponse diffId = new AccountResponse(2L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse diffName = new AccountResponse(1L, "Zoe", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse diffBalance = new AccountResponse(1L, "Eve", new BigDecimal("99.99"), "ACTIVE");
        AccountResponse diffStatus = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "CLOSED");

        assertNotEquals(baseline, diffId);
        assertNotEquals(baseline, diffName);
        assertNotEquals(baseline, diffBalance);
        assertNotEquals(baseline, diffStatus);
    }

    @Test
    void worksInHashBasedCollections() {
        AccountResponse a1 = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse a2 = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        Set<AccountResponse> set = new HashSet<>();
        set.add(a1);

        assertTrue(set.contains(a1));
        assertTrue(set.contains(a2)); // because equals/hashCode match
        assertEquals(1, set.size());
    }

    @Test
    void bigDecimalEqualityInEqualsUsesObjectsEquals() {
        // Your equals() uses Objects.equals(balance, that.balance), which relies on BigDecimal.equals
        // BigDecimal.equals considers scale: 12.340 != 12.34
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.340"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertNotEquals(a, b, "BigDecimal with different scales are not equal via equals()");
    }

    @Test
    void nullFieldHandlingInEquals() {
        AccountResponse a = new AccountResponse(null, null, null, null);
        AccountResponse b = new AccountResponse(null, null, null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void mutateFieldsShouldAffectEqualsAndHashCode() {
        AccountResponse a = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "Eve", new BigDecimal("12.34"), "ACTIVE");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        b.setStatus("SUSPENDED");

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsImplementationShouldMatchManualComparison() {
        AccountResponse a = new AccountResponse(1L, "A", new BigDecimal("1.00"), "ACTIVE");
        AccountResponse b = new AccountResponse(1L, "A", new BigDecimal("1.00"), "ACTIVE");

        boolean manual =
                Objects.equals(a.getId(), b.getId()) &&
                        Objects.equals(a.getHolderName(), b.getHolderName()) &&
                        Objects.equals(a.getBalance(), b.getBalance()) &&
                        Objects.equals(a.getStatus(), b.getStatus());

        assertEquals(a.equals(b), manual);
    }
}