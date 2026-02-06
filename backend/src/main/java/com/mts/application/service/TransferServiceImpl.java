package com.mts.application.service;
import com.mts.application.entities.Account;
import com.mts.application.entities.TransactionLog;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.application.service.AccountService;
import com.mts.application.service.TransferService;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.DuplicateTransferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service //
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountService accountService; //

    @Autowired
    private TransactionLogRepository logRepository; //

    @Override
    @Transactional // Ensures atomicity: if any step fails, everything rolls back
    public TransferResponse transfer(TransferRequest request) {

        // 1. Idempotency Check: Prevent duplicate processing
        if (logRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()) {
            throw new DuplicateTransferException("Duplicate transaction: " + request.getIdempotencyKey());
        }

        try {
            // 2. Validation: Check if accounts exist and are ACTIVE
            accountService.validateAccountForTransfer(request.getSourceAccountId());
            accountService.validateAccountForTransfer(request.getDestinationAccountId());

            // 3. Retrieval: Fetch account objects
            Account sender = accountService.getAccountById(request.getSourceAccountId());
            Account receiver = accountService.getAccountById(request.getDestinationAccountId());

            // 4. Execution: Debit and Credit (Insufficient balance check is inside debit)
            sender.debit(request.getAmount());
            receiver.credit(request.getAmount());

            // 5. Auditing: Log the success
            TransactionLog successLog = saveLog(request, TransactionStatus.SUCCESS, null);

            // 6. Return: Using your custom 2-argument constructor
            return new TransferResponse(successLog.getId().toString(), "Transfer Completed Successfully");

        } catch (Exception e) {
            // Log failure for auditing even if the account balance rolls back
            saveLog(request, TransactionStatus.FAILED, e.getMessage());
            throw e; // Rethrow to trigger @Transactional rollback
        }
    }

    private TransactionLog saveLog(TransferRequest req, TransactionStatus status, String reason) {
        TransactionLog log = new TransactionLog();
        log.setFromAccountId(req.getSourceAccountId());
        log.setToAccountId(req.getDestinationAccountId());
        log.setAmount(req.getAmount());
        log.setStatus(status);
        log.setFailureReason(reason);
        log.setIdempotencyKey(req.getIdempotencyKey());
        log.setCreatedOn(Instant.now());
        return logRepository.save(log); //
    }
}
