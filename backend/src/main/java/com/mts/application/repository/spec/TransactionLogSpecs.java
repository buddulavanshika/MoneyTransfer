package com.mts.application.repository.spec;

import com.mts.application.entities.TransactionLog;
import com.mts.domain.enums.TransactionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class TransactionLogSpecs {
    private TransactionLogSpecs() {}

    /**
     * Filter by account involved as sender or receiver.
     * accountId is the string representation of the account Long id.
     */
    public static Specification<TransactionLog> forAccount(Long accountId) {
        if (accountId == null) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> cb.or(
                cb.equal(root.get("fromAccountId"), accountId),
                cb.equal(root.get("toAccountId"), accountId)
        );
    }

    public static Specification<TransactionLog> createdOnFrom(Instant from) {
        return (root, q, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdOn"), from);
    }

    public static Specification<TransactionLog> createdOnTo(Instant to) {
        return (root, q, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdOn"), to);
    }

    public static Specification<TransactionLog> status(TransactionStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<TransactionLog> directionSentOnly(Long accountId) {
        if (accountId == null) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> cb.equal(root.get("fromAccountId"), accountId);
    }

    public static Specification<TransactionLog> directionReceivedOnly(Long accountId) {
        if (accountId == null) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> cb.equal(root.get("toAccountId"), accountId);
    }
}
