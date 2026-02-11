package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;

import java.math.BigDecimal;

public interface AccountService {
    Account getAccountById(String id) throws AccountNotFoundException;
    BigDecimal getBalance(String id) throws AccountNotFoundException;
    void createAccount(Account account);
    void validateAccountForTransfer(String id) throws AccountNotActiveException, AccountNotFoundException;
}
