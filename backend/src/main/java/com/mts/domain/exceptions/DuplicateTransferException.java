package com.mts.domain.exceptions;

public class DuplicateTransferException extends RuntimeException {

    public DuplicateTransferException() {
        super("Duplicate transfer detected");
    }

    public DuplicateTransferException(String message) {
        super(message);
    }

    public DuplicateTransferException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateTransferException(Throwable cause) {
        super(cause);
    }
}