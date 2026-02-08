-- Schema for Money Transfer System
-- Use with spring.jpa.defer-datasource-initialization and spring.sql.init if not using ddl-auto

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holder_name VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP(6) NULL,
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE IF NOT EXISTS transaction_logs (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500) NULL,
    created_on TIMESTAMP(6) NOT NULL,
    CONSTRAINT uq_transaction_logs_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_tx_from FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    CONSTRAINT fk_tx_to FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);
