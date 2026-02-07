package com.mts.application.controller;

import com.mts.application.service.TransferService;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Money transfer APIs")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Operation(
            summary = "Execute fund transfer",
            description = "Transfers money from source account to destination account"
    )
    @ApiResponse(responseCode = "200", description = "Transfer successful")
    @ApiResponse(responseCode = "400", description = "Invalid request / Insufficient balance")
    @ApiResponse(responseCode = "409", description = "Duplicate transfer")
    @PostMapping
    public ResponseEntity<TransferResponse> executeTransfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
