package com.mts.domain.model;

import com.mts.domain.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLogTest {

    @Test
    void noArgsConstructorShouldInitializeToNulls() {
        TransactionLog log = new TransactionLog();

        assertNull(log.getId());
        assertNull(log.getFromAccountId());
        assertNull(log.getToAccountId());
        assertNull(log.getAmount());
        assertNull(log.getStatus());
        assertNull(log.getFailureReason());
        assertNull(log.getIdempotencyKey());
        assertNull(log.getCreatedOn());
    }

    @Test
    void allArgsConstructorShouldInitializeAllFields() {
        String id = "txn-001";
        String from = "1001";
        String to = "2002";
        BigDecimal amount = new BigDecimal("99.95");
        TransactionStatus status = TransactionStatus.SUCCESS;
        String failureReason = null;
        String idempotencyKey = "idem-abc";
        Instant created = Instant.parse("2024-06-01T10:15:30Z");

        TransactionLog log = new TransactionLog(
                id, from, to, amount, status, failureReason, idempotencyKey, created
        );

        assertEquals(id, log.getId());
        assertEquals(from, log.getFromAccountId());
        assertEquals(to, log.getToAccountId());
        assertEquals(amount, log.getAmount());
        assertEquals(status, log.getStatus());
        assertNull(log.getFailureReason());
        assertEquals(idempotencyKey, log.getIdempotencyKey());
        assertEquals(created, log.getCreatedOn());
    }

    @Test
    void settersShouldUpdateFields() {
        TransactionLog log = new TransactionLog();

        log.setId("txn-42");
        log.setFromAccountId("A1");
        log.setToAccountId("B2");
        log.setAmount(new BigDecimal("10.00"));
        log.setStatus(TransactionStatus.SUCCESS);
        log.setFailureReason("Insufficient funds");
        log.setIdempotencyKey("idem-42");
        log.setCreatedOn(Instant.parse("2025-01-01T00:00:00Z"));

        assertAll(
                () -> assertEquals("txn-42", log.getId()),
                () -> assertEquals("A1", log.getFromAccountId()),
                () -> assertEquals("B2", log.getToAccountId()),
                () -> assertEquals(new BigDecimal("10.00"), log.getAmount()),
                () -> assertEquals(TransactionStatus.SUCCESS, log.getStatus()),
                () -> assertEquals("Insufficient funds", log.getFailureReason()),
                () -> assertEquals("idem-42", log.getIdempotencyKey()),
                () -> assertEquals(Instant.parse("2025-01-01T00:00:00Z"), log.getCreatedOn())
        );
    }

    @Test
    void toStringShouldContainKeyFields() {
        TransactionLog log = new TransactionLog(
                "txn-777",
                "111",
                "222",
                new BigDecimal("5.50"),
                TransactionStatus.FAILED,
                "Daily limit exceeded",
                "idem-777",
                Instant.parse("2024-12-31T23:59:59Z")
        );

        String s = log.toString();

        assertNotNull(s);
        assertTrue(s.contains("TransactionLog"));
    }

    @Test
    void canHandleNullsGracefully() {
        TransactionLog log = new TransactionLog(
                null, null, null, null, null, null, null, null
        );

        assertNull(log.getId());
        assertNull(log.getFromAccountId());
        assertNull(log.getToAccountId());
        assertNull(log.getAmount());
        assertNull(log.getStatus());
        assertNull(log.getFailureReason());
        assertNull(log.getIdempotencyKey());
        assertNull(log.getCreatedOn());
    }

    @Test
    void bigDecimalReferenceIsStoredAsIs_scaleSensitiveEquals() {
        TransactionLog log1 = new TransactionLog();
        log1.setAmount(new BigDecimal("1.0"));

        TransactionLog log2 = new TransactionLog();
        log2.setAmount(new BigDecimal("1.00"));

        assertNotEquals(log1.getAmount(), log2.getAmount(),
                "BigDecimal.equals is scale-sensitive: 1.0 != 1.00");
        assertEquals(0, log1.getAmount().compareTo(log2.getAmount()),
                "Numerically equal even if equals() differs");
    }

    @Test
    void mutationScenario_shouldReflectChanges() {
        TransactionLog log = new TransactionLog(
                "txn-10", "A", "B", new BigDecimal("10.00"),
                TransactionStatus.SUCCESS, null, "idem-10",
                Instant.parse("2024-01-01T00:00:00Z")
        );

        // mutate
        log.setStatus(TransactionStatus.FAILED);
        log.setFailureReason("Rejected");
        log.setAmount(new BigDecimal("12.34"));

        assertEquals(TransactionStatus.FAILED, log.getStatus());
        assertEquals("Rejected", log.getFailureReason());
        assertEquals(new BigDecimal("12.34"), log.getAmount());
    }
}