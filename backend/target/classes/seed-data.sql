-- Seed data: at least two active accounts with initial balances
-- Uses INSERT IGNORE so re-runs do not fail (MySQL)

INSERT IGNORE INTO accounts (id, holder_name, balance, status, version, last_updated) VALUES
(1, 'Alice', 1000.00, 'ACTIVE', 0, CURRENT_TIMESTAMP(6)),
(2, 'Bob', 500.00, 'ACTIVE', 0, CURRENT_TIMESTAMP(6));
