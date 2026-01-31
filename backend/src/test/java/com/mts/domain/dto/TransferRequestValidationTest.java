package com.mts.domain.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean Validation tests for TransferRequest DTO.
 *
 * Assumes:
 *  - @NotNull on fromAccountId, toAccountId, amount, idempotencyKey
 *  - @DecimalMin(value="0.01") (or similar) on amount
 *  - @NotBlank (or @Size(min=1)) on idempotencyKey
 *  - Custom cross-field check (optional) that from != to (validated in domain/service anyway)
 */
class TransferRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TransferRequest buildValid() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1L);
        req.setToAccountId(2L);
        req.setAmount(new BigDecimal("100.00"));
        req.setIdempotencyKey("IDEMP-VALID-001");
        return req;
    }

    @Test
    void testValidRequest() {
        TransferRequest req = buildValid();

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Valid request should produce no violations");
    }

    @Test
    void testInvalidAmount() {
        TransferRequest zero = buildValid();
        zero.setAmount(new BigDecimal("0.00"));

        Set<ConstraintViolation<TransferRequest>> v1 = validator.validate(zero);
        assertFalse(v1.isEmpty(), "Zero amount should be invalid");

        TransferRequest negative = buildValid();
        negative.setAmount(new BigDecimal("-10.00"));

        Set<ConstraintViolation<TransferRequest>> v2 = validator.validate(negative);
        assertFalse(v2.isEmpty(), "Negative amount should be invalid");
    }

    @Test
    void testNullFields() {
        TransferRequest req = new TransferRequest();
        // all fields null

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());

        // We expect at least 4 violations (fromAccountId, toAccountId, amount, idempotencyKey)
        assertTrue(violations.size() >= 4, "Expected violations for all required fields");
    }

    @Test
    void testBlankIdempotencyKey() {
        TransferRequest req = buildValid();
        req.setIdempotencyKey("   ");

        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Blank idempotency key should be invalid");
    }
}