package com.mts.application.controller;

import com.mts.application.service.TransferService;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import jakarta.validation.Valid; //
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers") //
public class TransferController {

    @Autowired
    private TransferService transferService; //

    @PostMapping // Task 8.4: Execute fund transfer
    public ResponseEntity<TransferResponse> executeTransfer(@Valid @RequestBody TransferRequest request) {
        // @Valid ensures DTO validation annotations are checked
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }
}