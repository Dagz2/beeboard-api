-- =============================================================================
-- Migration: V19__add_note_recipient_user_fk.sql
-- Description: Add foreign key constraint from note_recipient.user_id to author_view.user_id
-- Rationale: Maintain consistency with note/comment FK to author_view
-- =============================================================================

BEGIN;

-- Add FK constraint: note_recipient.user_id -> author_view.user_id
ALTER TABLE note_recipient
    ADD CONSTRAINT fk_note_recipient_user
    FOREIGN KEY (user_id) REFERENCES author_view(user_id)
    ON DELETE CASCADE;

COMMENT ON CONSTRAINT fk_note_recipient_user ON note_recipient IS
'FK to author_view (CQRS read model). ON DELETE CASCADE automatically removes share relationships when user is deleted from author_view. This ensures cleanup when UserDeletedEvent is processed.';

COMMIT;
