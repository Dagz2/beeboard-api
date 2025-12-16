BEGIN;
-- Migration script to create rate limit table for password reset requests
-- This table tracks the number of password reset requests per email to prevent abuse

-- Table: rate_limit_password_reset
CREATE TABLE rate_limit_password_reset (
    email VARCHAR(255) PRIMARY KEY NOT NULL,
    request_count INTEGER NOT NULL DEFAULT 1,
    window_start TIMESTAMP NOT NULL
);

-- Index for performance on window_start for cleanup queries
CREATE INDEX idx_rate_limit_window ON rate_limit_password_reset(window_start);

COMMIT;

END;
