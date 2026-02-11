package com.mts.application.service;
import com.mts.application.entities.Account;
import java.math.BigDecimal;


public interface AccountService {
    Account getAccountById(String id);
    BigDecimal getBalance(String id);
    void createAccount(Account account);
    void validateAccountForTransfer(String id);

}
