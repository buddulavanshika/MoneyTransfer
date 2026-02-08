package com.mts.application.entities;

import com.mts.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "transaction_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key")
)
public class TransactionLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;
}
