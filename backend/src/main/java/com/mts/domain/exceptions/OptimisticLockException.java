package com.mts.domain.exceptions;

/**
 * Thrown when a concurrent modification is detected (e.g. optimistic locking conflict).
 */
public class OptimisticLockException extends Exception {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
