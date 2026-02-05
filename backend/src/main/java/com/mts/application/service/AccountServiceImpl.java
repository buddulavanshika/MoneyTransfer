package com.mts.application.service.impl;

import com.mts.application.entities.Account;
import com.mts.application.repository.AccountRepository;
import com.mts.application.service.AccountService;
import com.mts.application.exception.AccountNotFoundException; //
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service //
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository; //

    @Override
    public Account getAccountById(String id) {
        // Replacing RuntimeException with your custom AccountNotFoundException
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + id + " not found"));
    }

    @Override
    public BigDecimal getBalance(String id) {
        // This will now automatically throw AccountNotFoundException if the ID is invalid
        return getAccountById(id).getBalance();
    }

    @Override
    @Transactional //
    public void createAccount(Account account) {
        accountRepository.save(account); //
    }
}