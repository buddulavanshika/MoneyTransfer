package com.mts.application.exceptions;

import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.exceptions.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import com.mts.domain.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // Prefer this to get JSON by default

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                "ACCOUNT_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request,
                null
        );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, HttpServletRequest request) {
        return buildErrorResponse(
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request,
                null
        );
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActive(
            AccountNotActiveException ex, HttpServletRequest request) {
        return buildErrorResponse(
                "ACCOUNT_NOT_ACTIVE",
                ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request,
                null
        );
    }

    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransfer(
            DuplicateTransferException ex, HttpServletRequest request) {
        return buildErrorResponse(
                "DUPLICATE_TRANSFER",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request,
                null
        );
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockException ex, HttpServletRequest request) {
        return buildErrorResponse(
                "CONCURRENT_MODIFICATION",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request,
                null
        );
    }

    // ===== Persistence / Idempotency conflicts =====

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        // You can inspect ex.getCause()/constraint name to be more specific if needed.
        Map<String, Object> details = Map.of(
                "reason", "Unique constraint violated (likely idempotency key)",
                "hint", "Reuse the same idempotency key only for the same request payload"
        );
        return buildErrorResponse(
                "DUPLICATE_REQUEST",
                "Request already processed with the same idempotency key",
                HttpStatus.CONFLICT,
                request,
                details
        );
    }

    // ===== Validation & binding =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);

        return buildErrorResponse(
                "VALIDATION_FAILED",
                "One or more fields are invalid",
                HttpStatusCode.valueOf(422),
                request,
                details
        );
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> violations = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            // propertyPath like "transfer.request.amount"
            violations.put(v.getPropertyPath().toString(), v.getMessage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("violations", violations);

        return buildErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Validation constraints violated",
                HttpStatusCode.valueOf(422),
                request,
                details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        Class<?> requiredType = ex.getRequiredType();

        Map<String, Object> details = Map.of(
                "name", ex.getName(),
                "requiredType", requiredType != null ? requiredType.getSimpleName() : null,
                "value", ex.getValue()
        );

        return buildErrorResponse(
                "TYPE_MISMATCH",
                "Request parameter type is invalid",
                HttpStatus.BAD_REQUEST,
                request,
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        // Often thrown for malformed JSON, wrong enum values, etc.
        return buildErrorResponse(
                "MALFORMED_REQUEST",
                "Request body is invalid or unreadable",
                HttpStatus.BAD_REQUEST,
                request,
                null
        );
    }

    // ===== Catch-all fallback =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        // Avoid leaking internal details; log full stack trace in a logger/aspect.
        return buildErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                null
        );
    }

    // ===== Helper & model =====

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String code,
            String message,
            HttpStatusCode status,
            HttpServletRequest request,
            Map<String, Object> details
    ) {
        ErrorResponse error = new ErrorResponse(
                code,
                message,
                status.value(),
                request != null ? request.getRequestURI() : null,
                OffsetDateTime.now().toString(),
                correlationId(request),
                details
        );
        return ResponseEntity.status(status).body(error);
    }

    private String correlationId(HttpServletRequest request) {
        if (request == null) return null;
        // If you add a filter that sets/propagates a correlation id header (e.g., X-Correlation-Id),
        // read it here to include in all error responses:
        String cid = request.getHeader("X-Correlation-Id");
        return (cid == null || cid.isBlank()) ? null : cid;
    }

}