package com.mts.domain.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountNotActiveExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Account is not active";
        AccountNotActiveException exception = new AccountNotActiveException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionInheritance() {
        AccountNotActiveException exception = new AccountNotActiveException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}