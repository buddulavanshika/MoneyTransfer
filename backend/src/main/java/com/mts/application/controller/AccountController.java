package com.mts.application.controller;

import com.mts.application.entities.Account;
import com.mts.application.service.AccountService;
import com.mts.domain.dto.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account related APIs")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Get account details by account ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        Account account = accountService.getAccountById(id); // expects String
        AccountResponse dto = toResponse(account);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get account balance")
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String id) {
        BigDecimal balance = accountService.getBalance(id); // expects String
        return ResponseEntity.ok(balance);
    }

    private AccountResponse toResponse(Account a) {
        AccountResponse dto = new AccountResponse();
        dto.setId(Long.valueOf(a.getId())); // both are String
        dto.setHolderName(a.getHolderName());
        dto.setBalance(a.getBalance());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        return dto;
    }
}