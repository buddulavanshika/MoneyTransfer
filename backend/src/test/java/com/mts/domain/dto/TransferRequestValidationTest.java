package com.mts.domain.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransferRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequestShouldPassValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1L);
        req.setToAccountId(2L);
        req.setAmount(new BigDecimal("10.00"));
        req.setIdempotencyKey("idem-123");

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(req);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullAmountShouldFailValidation() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1L);
        req.setToAccountId(2L);
        req.setAmount(null);
        req.setIdempotencyKey("idem-123");

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(req);

        assertFalse(violations.isEmpty());
    }

    @Test
    void amountLessThanMinimumShouldFail() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountId(1L);
        req.setToAccountId(2L);
        req.setAmount(new BigDecimal("0.00"));
        req.setIdempotencyKey("idem-123");

        Set<ConstraintViolation<TransferRequest>> violations =
                validator.validate(req);

        assertTrue(
            violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount"))
        );
    }
}
