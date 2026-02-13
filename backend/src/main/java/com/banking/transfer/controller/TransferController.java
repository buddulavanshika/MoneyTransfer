package com.banking.transfer.controller;

import com.banking.transfer.dto.TransactionResponse;
import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getHistory(@PathVariable Long accountId) {

        List<TransactionResponse> history =
                transferService.getTransactionHistory(accountId);

        return ResponseEntity.ok(history);
    }
}
