package com.mts.application.repository.spec;


import com.mts.application.entities.TransactionLog;
import com.mts.domain.enums.TransactionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public final class TransactionLogSpecs {
    private TransactionLogSpecs() {}

    public static Specification<TransactionLog> forAccount(String accountId) {
        return (root, q, cb) -> cb.or(
                cb.equal(root.get("fromAccountId"), accountId),
                cb.equal(root.get("toAccountId"), accountId)
        );
    }

    public static Specification<TransactionLog> createdOnFrom(OffsetDateTime from) {
        return (root, q, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdOn"), from);
    }

    public static Specification<TransactionLog> createdOnTo(OffsetDateTime to) {
        return (root, q, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdOn"), to);
    }

    public static Specification<TransactionLog> status(TransactionStatus status) {
        return (root, q, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<TransactionLog> directionSentOnly(String accountId) {
        return (root, q, cb) -> cb.equal(root.get("fromAccountId"), accountId);
    }

    public static Specification<TransactionLog> directionReceivedOnly(String accountId) {
        return (root, q, cb) -> cb.equal(root.get("toAccountId"), accountId);
    }
}
