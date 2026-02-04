package com.mts.domain.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InsufficientBalanceExceptionTest {

    @Test
    void testExceptionMessage() {
        String expectedMessage = "Insufficient balance for transfer";
        InsufficientBalanceException ex = new InsufficientBalanceException(expectedMessage);

        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testExceptionIsThrown() {
        assertThrows(InsufficientBalanceException.class, () -> {
            throw new InsufficientBalanceException("Balance too low");
        });
    }

    @Test
    void testExceptionWithCause() {
        Throwable cause = new RuntimeException("Underlying cause");
        InsufficientBalanceException ex = new InsufficientBalanceException("Balance too low", cause);

        assertEquals("Balance too low", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
