package com.mts.domain.dto;

import com.mts.domain.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransferResponseTest {

    @Test
    void constructorAndAccessorsShouldWork() {
        String transactionId = "txn-123";
        String sourceAccountId = "1001";
        String destinationAccountId = "2002";
        BigDecimal amount = new BigDecimal("250.00");
        String currency = "INR";
        TransactionStatus status = TransactionStatus.SUCCESS;
        String message = "Transfer completed";
        String idempotencyKey = "idem-xyz";
        Instant createdOn = Instant.now();

        TransferResponse resp = new TransferResponse(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                amount,
                currency,
                status,
                message,
                idempotencyKey,
                createdOn
        );

        assertEquals(transactionId, resp.transactionId());
        assertEquals(sourceAccountId, resp.sourceAccountId());
        assertEquals(destinationAccountId, resp.destinationAccountId());
        assertEquals(amount, resp.amount());
        assertEquals(currency, resp.currency());
        assertEquals(status, resp.status());
        assertEquals(message, resp.message());
        assertEquals(idempotencyKey, resp.idempotencyKey());
        assertEquals(createdOn, resp.createdOn());
    }

    @Test
    void equalsAndHashCodeShouldBeValueBased() {
        TransferResponse a = new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("1.00"), "USD",
                TransactionStatus.SUCCESS, "ok", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        );
        TransferResponse b = new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("1.00"), "USD",
                TransactionStatus.SUCCESS, "ok", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        );

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsShouldConsiderAllComponents() {
        TransferResponse base = new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        );

        assertNotEquals(base, new TransferResponse(
                "DIFF", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "DIFF", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "DIFF", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("11.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "EUR",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.FAILED, "done", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "DIFF", "idem-1", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "DIFF", Instant.parse("2024-01-01T00:00:00Z")
        ));
        assertNotEquals(base, new TransferResponse(
                "txn-1", "A", "B", new BigDecimal("10.00"), "USD",
                TransactionStatus.SUCCESS, "done", "idem-1", Instant.parse("2025-01-01T00:00:00Z")
        ));
    }

    @Test
    void toStringShouldContainAllComponents() {
        TransferResponse resp = new TransferResponse(
                "txn-42", "100", "200", new BigDecimal("5.50"), "INR",
                TransactionStatus.SUCCESS, "ok", "idem-42", Instant.parse("2024-06-01T10:15:30Z")
        );

        String s = resp.toString();
        assertTrue(s.contains("TransferResponse"));
        assertTrue(s.contains("txn-42"));
        assertTrue(s.contains("100"));
        assertTrue(s.contains("200"));
        assertTrue(s.contains("5.50"));
        assertTrue(s.contains("INR"));
        assertTrue(s.contains("SUCCESS"));
        assertTrue(s.contains("ok"));
        assertTrue(s.contains("idem-42"));
        assertTrue(s.contains("2024-06-01T10:15:30Z"));
    }

    @Test
    void recordIsImmutable_NoSettersExist() throws NoSuchMethodException {
        Class<TransferResponse> cls = TransferResponse.class;
        assertThrows(NoSuchMethodException.class, () -> cls.getMethod("setTransactionId", String.class));
        assertThrows(NoSuchMethodException.class, () -> cls.getMethod("setAmount", BigDecimal.class));
    }

    @Test
    void bigDecimalEqualityIsScaleSensitiveByDefault() {
        TransferResponse a = new TransferResponse(
                "id", "A", "B", new BigDecimal("1.0"), "USD",
                TransactionStatus.SUCCESS, "m", "i", Instant.EPOCH
        );
        TransferResponse b = new TransferResponse(
                "id", "A", "B", new BigDecimal("1.00"), "USD",
                TransactionStatus.SUCCESS, "m", "i", Instant.EPOCH
        );
        assertNotEquals(a, b, "1.0 != 1.00 via BigDecimal.equals (scale matters)");
    }

    @Test
    void nullToleranceIfAllowedByDesign() {
        TransferResponse resp = new TransferResponse(
                null, null, null, null, null, null, null, null, null
        );
        assertNull(resp.transactionId());
        assertNull(resp.sourceAccountId());
        assertNull(resp.destinationAccountId());
        assertNull(resp.amount());
        assertNull(resp.currency());
        assertNull(resp.status());
        assertNull(resp.message());
        assertNull(resp.idempotencyKey());
        assertNull(resp.createdOn());
    }
}