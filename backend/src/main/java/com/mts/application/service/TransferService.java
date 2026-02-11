package com.mts.application.service;

import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.dto.TransactionLogResponse;
import com.mts.domain.enums.TransactionStatus;
import com.mts.domain.exceptions.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface TransferService {

    TransferResponse transfer(TransferRequest request)
            throws AccountNotFoundException,
            AccountNotActiveException,
            InsufficientBalanceException,
            DuplicateTransferException,
            OptimisticLockException;

    enum Direction { ALL, SENT, RECEIVED }

    Page<TransactionLogResponse> getAccountTransactions(
            String accountId,
            Instant from,
            Instant to,
            TransactionStatus status,
            Direction direction,
            Pageable pageable
    );
}
