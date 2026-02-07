package com.mts.application.repository;

import com.mts.application.entities.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionLogRepository
        extends JpaRepository<TransactionLog, Long>, JpaSpecificationExecutor<TransactionLog> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);
}