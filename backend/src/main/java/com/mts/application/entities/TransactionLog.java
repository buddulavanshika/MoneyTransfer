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
@Table(
    name = "transaction_logs",
    indexes = {
        @Index(name = "idx_from_account", columnList = "fromAccountId"),
        @Index(name = "idx_to_account", columnList = "toAccountId"),
        @Index(name = "idx_idempotency", columnList = "idempotencyKey")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String fromAccountId;

    @Column(nullable = false)
    private String toAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 255)
    private String failureReason;

    @Column(unique = true, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdOn;

    @PrePersist
    public void onCreate() {
        this.createdOn = Instant.now();
    }
}
