BEGIN;
-- Migration script to create password reset tokens table
-- This table stores password reset tokens with expiration and usage tracking

-- Table: password_reset_tokens
CREATE TABLE password_reset_tokens (
    request_id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index for performance on user_id lookups
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);

-- Index for performance on token_hash lookups
CREATE INDEX idx_password_reset_token_hash ON password_reset_tokens(token_hash);

-- Index for performance on expiration queries
CREATE INDEX idx_password_reset_expires_at ON password_reset_tokens(expires_at);

-- Composite index for cleanup queries (expired and used tokens)
CREATE INDEX idx_password_reset_expired ON password_reset_tokens(expires_at, is_used);

COMMIT;

END;
