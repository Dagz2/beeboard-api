BEGIN;

-- =============================================================================
-- Migration: V16__create_author_view.sql
-- Description: CQRS Read Model - Materialized view of user data from Access BC
-- Pattern: Event-driven synchronization with Anti-Corruption Layer
-- Vocabulary: Note BC (access_level, not application_role)
-- =============================================================================

-- =============================================================================
-- Table: author_view
-- Purpose: Local read model for user data synchronized from Access BC via events
-- Design: No FK to Access BC - maintains bounded context independence
-- =============================================================================

CREATE TABLE author_view (
    user_id UUID PRIMARY KEY NOT NULL,
    access_level VARCHAR(20) NOT NULL,           -- Note BC vocabulary: SUPERVISOR/MODERATOR/COMMENTER/VIEWER
    account_status VARCHAR(20) NOT NULL,         -- Published Language: ACTIVE/LOCKED/EXPIRED
    full_name VARCHAR(200) NOT NULL,             -- For display in NoteDto/CommentDto
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_access_level CHECK (access_level IN ('SUPERVISOR', 'MODERATOR', 'COMMENTER', 'VIEWER')),
    CONSTRAINT chk_account_status CHECK (account_status IN ('ACTIVE', 'LOCKED', 'EXPIRED'))
);

-- =============================================================================
-- Indexes for common query patterns
-- =============================================================================

-- Index for filtering by access level (e.g., find all supervisors)
CREATE INDEX idx_author_view_access_level ON author_view(access_level);

-- Index for filtering by account status (e.g., find all active users)
CREATE INDEX idx_author_view_status ON author_view(account_status);

-- Index for searching by name (e.g., autocomplete in UI)
CREATE INDEX idx_author_view_full_name ON author_view(full_name);

-- =============================================================================
-- Table and column comments for documentation
-- =============================================================================

COMMENT ON TABLE author_view IS 'CQRS Read Model: User data synchronized from Access BC via domain events. No FK to Access BC - maintains bounded context independence.';

COMMENT ON COLUMN author_view.user_id IS 'User unique identifier (matches Access BC user.id). Primary key, no foreign key constraint.';

COMMENT ON COLUMN author_view.access_level IS 'Note BC vocabulary: SUPERVISOR (full privileges), MODERATOR (content management), COMMENTER (standard participation), VIEWER (read-only). Translated from ApplicationRole in UserEventHandler (Anti-Corruption Layer).';

COMMENT ON COLUMN author_view.account_status IS 'Published Language: ACTIVE (user can act), LOCKED (user suspended), EXPIRED (contract ended). Shared enum between BCs.';

COMMENT ON COLUMN author_view.full_name IS 'User full name for display in NoteDto/CommentDto author field. Enriches responses without additional queries.';

COMMENT ON COLUMN author_view.created_at IS 'Timestamp when the author_view entry was first created. Never updated.';

COMMENT ON COLUMN author_view.updated_at IS 'Timestamp when the author_view entry was last modified. Auto-updated by trigger.';

-- =============================================================================
-- Trigger: Auto-update updated_at timestamp
-- =============================================================================

CREATE OR REPLACE FUNCTION update_author_view_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_author_view_updated_at
    BEFORE UPDATE ON author_view
    FOR EACH ROW
    EXECUTE FUNCTION update_author_view_updated_at();

COMMIT;
