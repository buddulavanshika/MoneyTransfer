package com.mts.domain.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void constructorShouldInitializeFieldsCorrectly() {
        ErrorResponse errorResponse =
                new ErrorResponse("ERR_001", "Invalid transfer amount");

        assertEquals("ERR_001", errorResponse.getErrorCode());
        assertEquals("Invalid transfer amount", errorResponse.getMessage());
    }

    @Test
    void settersShouldUpdateFieldsCorrectly() {
        ErrorResponse errorResponse =
                new ErrorResponse("ERR_001", "Invalid transfer");

        errorResponse.setErrorCode("ERR_002");
        errorResponse.setMessage("Insufficient balance");

        assertEquals("ERR_002", errorResponse.getErrorCode());
        assertEquals("Insufficient balance", errorResponse.getMessage());
    }

    @Test
    void toStringShouldContainFieldValues() {
        ErrorResponse errorResponse =
                new ErrorResponse("ERR_003", "Account not found");

        String toString = errorResponse.toString();

        assertTrue(toString.contains("ERR_003"));
        assertTrue(toString.contains("Account not found"));
    }

    @Test
    void shouldAllowNullValuesIfSetExplicitly() {
        ErrorResponse errorResponse =
                new ErrorResponse(null, null);

        assertNull(errorResponse.getErrorCode());
        assertNull(errorResponse.getMessage());
    }
}