package com.mts.application.repository;

import com.mts.application.entities.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionLogRepository
        extends JpaRepository<TransactionLog, String>, JpaSpecificationExecutor<TransactionLog> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);
}
