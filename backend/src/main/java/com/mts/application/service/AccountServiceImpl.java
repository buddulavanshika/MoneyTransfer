package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.repository.AccountRepository;
import com.mts.application.service.AccountService;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException; //
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account with ID " + id + " not found")
                );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String id) {
        return getAccountById(id).getBalance();
    }

    @Override
    @Transactional
    public void createAccount(Account account) {

        if (accountRepository.existsById(account.getId())) {
            throw new IllegalStateException("Account already exists: " + account.getId());
        }

        if (account.getBalance() == null || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be >= 0");
        }

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void validateAccountForTransfer(String id) {

        Account account = accountRepository.findWithLockById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account with ID " + id + " not found")
                );

        if (!account.isActive()) {
            throw new AccountNotActiveException(
                    "Account " + id + " is currently " + account.getStatus()
            );
        }
    }
}
