
-- Seed a default user (if table/columns exist)
INSERT INTO users (id, username, password, enabled)
VALUES (1, 'admin', '{noop}admin', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);
CREATE TABLE IF NOT EXISTS roles (
  id   BIGINT      NOT NULL PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE
);