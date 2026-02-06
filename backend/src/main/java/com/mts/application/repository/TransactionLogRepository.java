package com.mts.application.repository;

import com.mts.application.entities.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionLogRepository extends JpaRepository<TransactionLog,String> {
    //logic to prevent duplicates

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);
}
