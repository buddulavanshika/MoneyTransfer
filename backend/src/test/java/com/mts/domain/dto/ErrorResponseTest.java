package com.mts.domain.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    private ErrorResponse makeErrorResponse() {
        return new ErrorResponse(
                "ERR_001",                 // code
                "Invalid transfer amount", // message
                422,                       // status
                "/transfers",              // path
                "2025-01-01T10:00:00Z",    // timestamp (string per your DTO)
                "cid-123",                 // correlationId
                Map.of("field", "amount")  // details
        );
    }

    @Test
    void constructorShouldInitializeFieldsCorrectly() {
        ErrorResponse errorResponse = makeErrorResponse();

        assertEquals("ERR_001", errorResponse.getCode());
        assertEquals("Invalid transfer amount", errorResponse.getMessage());
        assertEquals(422, errorResponse.getStatus());
        assertEquals("/transfers", errorResponse.getPath());
        assertEquals("2025-01-01T10:00:00Z", errorResponse.getTimestamp());
        assertEquals("cid-123", errorResponse.getCorrelationId());
        assertNotNull(errorResponse.getDetails());
        assertEquals("amount", errorResponse.getDetails().get("field"));
    }

    @Test
    void shouldAllowNullValuesIfProvided() {
        ErrorResponse errorResponse = new ErrorResponse(
                null,    // code
                null,    // message
                0,       // status (int cannot be null)
                null,    // path
                null,    // timestamp
                null,    // correlationId
                null     // details
        );

        assertNull(errorResponse.getCode());
        assertNull(errorResponse.getMessage());
        assertEquals(0, errorResponse.getStatus());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getTimestamp());
        assertNull(errorResponse.getCorrelationId());
        assertNull(errorResponse.getDetails());
    }

    @Test
    void gettersShouldReturnExactlyWhatWasPassedIn() {
        Map<String, Object> details = Map.of("reason", "validation");
        ErrorResponse errorResponse = new ErrorResponse(
                "ERR_002",
                "Insufficient balance",
                400,
                "/payments",
                "2025-01-01T12:00:00Z",
                "cid-999",
                details
        );

        assertEquals("ERR_002", errorResponse.getCode());
        assertEquals("Insufficient balance", errorResponse.getMessage());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("/payments", errorResponse.getPath());
        assertEquals("2025-01-01T12:00:00Z", errorResponse.getTimestamp());
        assertEquals("cid-999", errorResponse.getCorrelationId());
        assertSame(details, errorResponse.getDetails());
    }

    @Test
    void toStringShouldBeNonNullButIsNotAssertedForContent() {
        // Since ErrorResponse does not override toString(), we only check it's not null
        // to avoid coupling the test to Object#toString default format.
        ErrorResponse errorResponse = makeErrorResponse();
        assertNotNull(errorResponse.toString());
    }
}
