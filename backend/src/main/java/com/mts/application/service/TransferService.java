package com.mts.application.service;

import com.mts.application.entities.TransactionLog;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface TransferService {

    TransferResponse transfer(TransferRequest request);

    enum Direction { ALL, SENT, RECEIVED }

    Page<TransactionLog> getAccountTransactions(
            String accountId,
            OffsetDateTime from,
            OffsetDateTime to,
            TransactionStatus status,
            Direction direction,
            Pageable pageable
    );
}