package com.mts.domain.enums;

/**
 * Represents the lifecycle status of an Account in the domain model.
 *
 * ACTIVE  - Account is operational; debit/credit operations allowed.
 * LOCKED  - Account is temporarily disabled; no debit/credit operations.
 * CLOSED  - Account is permanently closed; no further operations allowed.
 */
public enum AccountStatus {
    ACTIVE,
    LOCKED,
    CLOSED
}