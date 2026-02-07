package com.mts.application.controller;

import com.mts.application.service.AccountService;
import com.mts.domain.dto.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account related APIs")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Operation(summary = "Get account details by account ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }
}
