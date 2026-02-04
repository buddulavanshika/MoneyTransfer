package com.mts.domain.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateTransferExceptionTest {

    @Test
    void testExceptionMessage() {
        String expectedMessage = "Duplicate transfer detected with idempotency key ABC123";
        DuplicateTransferException ex = new DuplicateTransferException(expectedMessage);

        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testExceptionIsThrown() {
        assertThrows(DuplicateTransferException.class, () -> {
            throw new DuplicateTransferException("Duplicate transfer");
        });
    }

    @Test
    void testExceptionWithCause() {
        Throwable cause = new RuntimeException("Underlying cause");
        DuplicateTransferException ex = new DuplicateTransferException("Duplicate transfer", cause);

        assertEquals("Duplicate transfer", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
