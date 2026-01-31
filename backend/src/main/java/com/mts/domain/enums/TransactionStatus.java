package com.mts.domain.enums;

/**
 * Represents the final outcome of a money transfer transaction.
 *
 * SUCCESS - The transfer completed and funds were correctly
 *           debited from the source and credited to the destination.
 *
 * FAILED  - The transfer did not complete successfully due to
 *           validation errors, insufficient funds, invalid status,
 *           or any other domain exception.
 */
public enum TransactionStatus {
    SUCCESS,
    FAILED
}