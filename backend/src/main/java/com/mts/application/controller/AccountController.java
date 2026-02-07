package com.mts.application.controller;

import com.mts.application.entities.Account;
import com.mts.application.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts") //
public class AccountController {

    @Autowired
    private AccountService accountService; //

    @GetMapping("/{id}") // Task 8.4: Get account details
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/{id}/balance") // Task 8.4: Get account balance
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
}