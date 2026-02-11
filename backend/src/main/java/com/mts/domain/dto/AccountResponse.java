package com.mts.domain.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Placeholder for AccountResponse (Module 2).
 */
public class AccountResponse {
    private Long id;
    private String holderName;
    private BigDecimal balance;
    private String status;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String holderName, BigDecimal balance, String status) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
    }

    public AccountResponse(Long id, String holderName) {
        this.id = id;
        this.holderName = holderName;
    }

    public AccountResponse(Long id) {
        this.id = id;
    }

    public AccountResponse(BigDecimal balance, String status) {
        this.balance = balance;
        this.status = status;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AccountResponse{" +
                "id=" + id +
                ", holderName='" + holderName + '\'' +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                '}';
    }

    // (Optional) Useful for collections/comparisons
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountResponse)) return false;
        AccountResponse that = (AccountResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(holderName, that.holderName) &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, holderName, balance, status);
    }
}
