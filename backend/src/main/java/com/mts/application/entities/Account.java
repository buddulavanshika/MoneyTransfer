package com.mts.application.entities;
import com.mts.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private String id;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Version
    private long version;

    private Instant lastUpdated;

    // --- Core Logic Methods ---
    public void debit(BigDecimal amount) {
        ensureActive(); //
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance"); //
        }
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    public void credit(BigDecimal amount) {
        ensureActive(); //
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        this.lastUpdated = Instant.now();
    }

    private void ensureActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active"); //
        }
    }
}