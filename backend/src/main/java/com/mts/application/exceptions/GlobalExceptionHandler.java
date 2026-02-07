package com.mts.application.exceptions;

import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import com.mts.domain.exceptions.DuplicateTransferException;
import com.mts.domain.exceptions.InsufficientBalanceException;
import com.mts.domain.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // Prefer this to get JSON by default

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return buildErrorResponse("ACCOUNT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return buildErrorResponse("INSUFFICIENT_BALANCE", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActive(AccountNotActiveException ex) {
        return buildErrorResponse("ACCOUNT_NOT_ACTIVE", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateTransferException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransfer(DuplicateTransferException ex) {
        return buildErrorResponse("DUPLICATE_TRANSFER", ex.getMessage(), HttpStatus.CONFLICT);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String code, String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(code, message);
        return ResponseEntity.status(status).body(error);
    }
}
