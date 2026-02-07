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
@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionLogRepository logRepository;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // 0. Self-transfer guard
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        // 1. Idempotency fast check (NOT final authority)
        if (logRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()) {
            throw new DuplicateTransferException(
                    "Duplicate transaction: " + request.getIdempotencyKey()
            );
        }

        TransactionLog log = null;

        try {
            // 2. Lock & validate accounts
            Account sender = accountService.getAccountForUpdate(request.getSourceAccountId());
            Account receiver = accountService.getAccountForUpdate(request.getDestinationAccountId());

            // 3. Execute transfer
            sender.debit(request.getAmount());
            receiver.credit(request.getAmount());

            // 4. Log success
            log = buildLog(request, TransactionStatus.SUCCESS, null);
            logRepository.save(log);

            return new TransferResponse(
                    log.getId(),
                    "Transfer completed successfully"
            );

        } catch (Exception ex) {

            // 5. Persist failure log in NEW transaction
            persistFailureLog(request, ex.getMessage());

            throw ex;
        }
    }

    private TransactionLog buildLog(
            TransferRequest req,
            TransactionStatus status,
            String reason) {

        TransactionLog log = new TransactionLog();
        log.setId(java.util.UUID.randomUUID().toString());
        log.setFromAccountId(req.getSourceAccountId());
        log.setToAccountId(req.getDestinationAccountId());
        log.setAmount(req.getAmount());
        log.setStatus(status);
        log.setFailureReason(reason);
        log.setIdempotencyKey(req.getIdempotencyKey());
        return log;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistFailureLog(TransferRequest req, String reason) {
        TransactionLog log = buildLog(req, TransactionStatus.FAILED, reason);
        logRepository.save(log);
    }
    @Transactional
    public Account getAccountForUpdate(String id) {
        return accountRepository.findWithLockById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
}

}
