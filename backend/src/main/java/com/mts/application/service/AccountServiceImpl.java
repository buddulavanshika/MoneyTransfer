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
    @Transactional
    public void createAccount(Account account) {
        accountRepository.save(account);

    }

    //used to validate the sender and receiver accounts before the transaction begins
    @Override
    public void validateAccountForTransfer(String id){
        Account account=getAccountById(id);
        if(!account.isActive()){
            throw new AccountNotActiveException("Account "+id+" is currently "+account.getStatus());
        }

    }
}