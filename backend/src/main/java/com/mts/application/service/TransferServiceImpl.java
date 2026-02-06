package com.mts.application.service;
import com.mts.application.entities.Account;
import com.mts.application.entities.TransactionLog;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.DuplicateTransferException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountService accountService; //

    @Autowired
    private TransactionLogRepository logRepository; //

    @Override
    @Transactional // Ensures atomicity: all or nothing
    public TransferResponse transfer(TransferRequest request) {
        // 1. Idempotency Check
        if (logRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()) {
            throw new DuplicateTransferException("Transaction already processed"); //
        }

        try {
            // 2. Validation
            accountService.validateAccountForTransfer(request.getSourceAccountId());
            accountService.validateAccountForTransfer(request.getDestinationAccountId());

            // 3. Execution
            Account sender = accountService.getAccountById(request.getSourceAccountId());
            Account receiver = accountService.getAccountById(request.getDestinationAccountId());

            sender.debit(request.getAmount());
            receiver.credit(request.getAmount());

            // 4. Log Success
            saveLog(request, TransactionStatus.SUCCESS, null);

            return new TransferResponse("SUCCESS", "Transfer Completed"); //

        } catch (Exception e) {
            // 5. Log Failure (but still throw exception for @Transactional to rollback)
            saveLog(request, TransactionStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    private void saveLog(TransferRequest req, TransactionStatus status, String reason) {
        TransactionLog log = TransactionLog.builder()
                .fromAccountId(req.getFromAccountId())
                .toAccountId(req.getToAccountId())
                .amount(req.getAmount())
                .status(status)
                .failureReason(reason)
                .idempotencyKey(req.getIdempotencyKey())
                .createdOn(Instant.now())
                .build();
        logRepository.save(log); //
    }
}