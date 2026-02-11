package com.mts.application.entities;

import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.InsufficientBalanceException;
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

    public void debit(BigDecimal amount) throws AccountNotActiveException,InsufficientBalanceException{
        ensureActive();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in account: " + this.id);
        }
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    public void credit(BigDecimal amount) throws AccountNotActiveException {
        ensureActive();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    private void ensureActive() throws AccountNotActiveException{
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + this.id + " is not ACTIVE");
        }
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
