package com.mts.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Test-only handler so that in @WebMvcTest slice, BadCredentialsException from
 * login returns 401 instead of 500 (slice does not load full security filters).
 */
@ControllerAdvice
public class TestAuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials() {
        return ResponseEntity.status(401).build();
    }
}
