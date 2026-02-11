package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.repository.AccountRepository;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(String id) {
        Long idLong = parseAccountId(id);
        return accountRepository.findById(idLong)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String id) {
        return getAccountById(id).getBalance();
    }

    @Override
    @Transactional
    public void createAccount(Account account) {
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateAccountForTransfer(String id) {
        Account account = getAccountById(id);
        if (!account.isActive()) {
            throw new AccountNotActiveException("Account " + id + " is not ACTIVE (status=" + account.getStatus() + ")");
        }
    }

    private static Long parseAccountId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Account id must not be blank");
        }
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException e) {
            throw new AccountNotFoundException("Invalid account id: " + id);
        }
    }
}
