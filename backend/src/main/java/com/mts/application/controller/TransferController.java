package com.mts.application.controller;

import com.mts.application.service.TransferService;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.dto.TransactionLogResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1") // ⬅️ moved here so we can expose both /transfers and /accounts/{id}/transactions
@Tag(name = "Transfers", description = "Endpoints for executing fund transfers and querying transaction history")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    // -------------------------
    // POST /api/v1/transfers
    // -------------------------
    @Operation(
            summary = "Execute a fund transfer",
            description = "Debits the source account and credits the destination account atomically. "
                    + "Validates account status, balance, and enforces idempotency if header is provided.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = TransferResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Account not active", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Duplicate transfer (idempotency)", content = @Content)
            }
    )
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> executeTransfer (
            @Valid @RequestBody
            @Parameter(description = "Transfer details (from, to, amount, currency, idempotencyKey)")
            TransferRequest request,

            @RequestHeader(value = "Idempotency-Key", required = false)
            @Parameter(description = "Optional idempotency key header to prevent duplicate transfers")
            String idempotencyKey
    )throws AccountNotFoundException, AccountNotActiveException, InsufficientBalanceException, DuplicateTransferException, OptimisticLockException {
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------
    // GET /api/v1/accounts/{id}/transactions (history endpoint)
    // ----------------------------------------------------------
    @Operation(
            summary = "Get transaction history for an account",
            description = "Returns paginated transactions where the account is either sender or receiver. "
                    + "Supports date range, status, and direction filters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of transactions",
                            content = @Content(schema = @Schema(implementation = TransactionLogResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid filters or pagination parameters", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Account not found (if you enforce existence check)", content = @Content)
            }
    )
    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<Page<TransactionLogResponse>> getTransactionHistory(
            @Parameter(name = "id", description = "Account ID (string)", in = ParameterIn.PATH, required = true)
            @PathVariable("id") String accountId,

            @Parameter(description = "Start timestamp (inclusive) in ISO-8601, e.g., 2026-02-01T00:00:00Z")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @Parameter(description = "End timestamp (inclusive) in ISO-8601, e.g., 2026-02-28T23:59:59Z")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @Parameter(description = "Transaction status filter",
                    schema = @Schema(implementation = TransactionStatus.class))
            @RequestParam(value = "status", required = false)
            TransactionStatus status,

            @Parameter(description = "Direction filter: ALL (default), SENT, RECEIVED",
                    schema = @Schema(allowableValues = {"ALL", "SENT", "RECEIVED"}))
            @RequestParam(value = "direction", required = false, defaultValue = "ALL")
            TransferService.Direction direction,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdOn,desc") Pageable pageable
    ) {
        Instant fromInstant = from != null ? from.toInstant() : null;
        Instant toInstant = to != null ? to.toInstant() : null;
        Page<TransactionLogResponse> page = transferService.getAccountTransactions(
                accountId, fromInstant, toInstant, status, direction, pageable
        );
        return ResponseEntity.ok(page);
    }
}