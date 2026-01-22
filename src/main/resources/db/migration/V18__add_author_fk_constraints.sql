-- =============================================================================
-- Migration: V18__add_author_fk_constraints.sql
-- Description: Add foreign key constraints from note/comment to author_view
-- Rationale: Ensure referential integrity without JPA relationships
-- =============================================================================

BEGIN;

-- Add FK constraint: note.author_id -> author_view.user_id
ALTER TABLE note
    ADD CONSTRAINT fk_note_author
    FOREIGN KEY (author_id) REFERENCES author_view(user_id)
    ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_note_author ON note IS
'FK to author_view (CQRS read model). ON DELETE RESTRICT prevents orphaned notes if author is deleted from author_view. Use case: when UserDeletedEvent is received, first anonymize/delete notes, then delete from author_view.';

-- Add FK constraint: comment.author_id -> author_view.user_id
ALTER TABLE comment
    ADD CONSTRAINT fk_comment_author
    FOREIGN KEY (author_id) REFERENCES author_view(user_id)
    ON DELETE RESTRICT;

COMMENT ON CONSTRAINT fk_comment_author ON comment IS
'FK to author_view (CQRS read model). ON DELETE RESTRICT prevents orphaned comments if author is deleted from author_view. Use case: when UserDeletedEvent is received, first anonymize/delete comments, then delete from author_view.';

COMMIT;
