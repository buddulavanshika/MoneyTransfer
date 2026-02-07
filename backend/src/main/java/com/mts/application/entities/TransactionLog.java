package com.mts.application.entities;
import com.mts.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "transaction_logs",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "idempotency_key"
        )
)
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private Long fromAccountId;
    private Long toAccountId;

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String failureReason;

    private OffsetDateTime createdOn = OffsetDateTime.now();
}