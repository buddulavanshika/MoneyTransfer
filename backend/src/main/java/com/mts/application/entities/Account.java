package com.mts.application.entities;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Version
    private long version;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    void touch() {
        this.lastUpdated = Instant.now();
    }

    public void debit(BigDecimal amount) {
        ensureActive();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    public void credit(BigDecimal amount) {
        ensureActive();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    private void ensureActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + id + " is not ACTIVE");
        }
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
