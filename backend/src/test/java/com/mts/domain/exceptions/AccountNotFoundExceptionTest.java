package com.mts.domain.exceptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountNotFoundExceptionTest{

    @Test
    void testExceptionMessage() {
        String expectedMessage = "Account not found with ID 123";
        AccountNotFoundException ex = new AccountNotFoundException(expectedMessage);
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testExceptionIsThrown() {
        assertThrows(AccountNotFoundException.class, () -> {
            throw new AccountNotFoundException("Account missing");
        });
    }

    @Test
    void testExceptionWithCause() {
        Throwable cause = new RuntimeException("Underlying cause");
        AccountNotFoundException ex = new AccountNotFoundException("Account missing", cause);

        assertEquals("Account missing", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }


}