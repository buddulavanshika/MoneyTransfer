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
public class Account {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    private long version;

    @Column(nullable = false)
    private Instant lastUpdated;

    @PrePersist
    public void onCreate() {
        this.lastUpdated = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = Instant.now();
    }

    public void debit(BigDecimal amount) {
        ensureAccountIsActive();
        validateAmount(amount);

        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                "Insufficient balance in account " + id
            );
        }

        this.balance = this.balance.subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void credit(BigDecimal amount) {
        ensureAccountIsActive();
        validateAmount(amount);

        this.balance = this.balance.add(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureAccountIsActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                "Account " + id + " is not active"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }
}
