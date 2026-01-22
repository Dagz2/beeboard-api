BEGIN;

-- =============================================================================
-- Note Bounded Context - Complete Schema
-- Creates all tables for the Note BC with proper relational structure
-- NO JSONB - All collections stored in separate tables
-- =============================================================================

-- =============================================================================
-- Table: tag
-- Description: Tag aggregate root for classifying notes
-- =============================================================================
CREATE TABLE tag (
    tag_id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    usage_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tag_name_format CHECK (name ~ '^[a-z0-9-]+$'),
    CONSTRAINT chk_usage_count_non_negative CHECK (usage_count >= 0)
);

CREATE INDEX idx_tag_name ON tag(name);
CREATE INDEX idx_tag_usage_count ON tag(usage_count);

COMMENT ON TABLE tag IS 'Tags for flexible classification of notes';
COMMENT ON COLUMN tag.name IS 'Unique tag name (lowercase, alphanumeric with hyphens)';
COMMENT ON COLUMN tag.usage_count IS 'Number of notes using this tag';

-- =============================================================================
-- Table: note
-- Description: Note aggregate root
-- =============================================================================
CREATE TABLE note (
    note_id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    content_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    category VARCHAR(50) NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    author_id UUID NOT NULL,
    project_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    archived_at TIMESTAMP,

    CONSTRAINT chk_content_length CHECK (LENGTH(content) <= 50000),
    CONSTRAINT chk_title_length CHECK (LENGTH(title) <= 200),
    CONSTRAINT chk_content_type CHECK (content_type IN ('TEXT', 'CODE', 'IMAGE', 'MIXED')),
    CONSTRAINT chk_category CHECK (category IN ('BUG', 'TIP', 'DOCUMENTATION', 'QUESTION', 'ANNOUNCEMENT', 'RECOMMENDATION', 'BUSINESS_RULE', 'CODE_SNIPPET')),
    CONSTRAINT chk_visibility CHECK (visibility IN ('PRIVATE', 'SHARED', 'PROJECT')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'DELETED')),
    CONSTRAINT chk_project_required_for_project_visibility CHECK (
        (visibility = 'PROJECT' AND project_id IS NOT NULL) OR
        (visibility != 'PROJECT')
    )
);

-- Indexes for performance
CREATE INDEX idx_note_author_id ON note(author_id);
CREATE INDEX idx_note_category ON note(category);
CREATE INDEX idx_note_visibility ON note(visibility);
CREATE INDEX idx_note_status ON note(status);
CREATE INDEX idx_note_project_id ON note(project_id) WHERE project_id IS NOT NULL;
CREATE INDEX idx_note_author_active ON note(author_id, status) WHERE status = 'ACTIVE';
CREATE INDEX idx_note_archived_at ON note(archived_at) WHERE archived_at IS NOT NULL;
CREATE INDEX idx_note_fulltext ON note USING GIN (to_tsvector('english', title || ' ' || content));

COMMENT ON TABLE note IS 'Notes for knowledge sharing and discussion (Note BC aggregate root)';
COMMENT ON COLUMN note.title IS 'Note title (max 200 characters)';
COMMENT ON COLUMN note.content IS 'Note body content (max 50,000 characters)';
COMMENT ON COLUMN note.content_type IS 'Type of content: TEXT, CODE, IMAGE, or MIXED';
COMMENT ON COLUMN note.category IS 'Mandatory category: BUG, TIP, DOCUMENTATION, QUESTION, ANNOUNCEMENT, RECOMMENDATION, BUSINESS_RULE, CODE_SNIPPET';
COMMENT ON COLUMN note.visibility IS 'Who can see this note: PRIVATE, SHARED, PROJECT';
COMMENT ON COLUMN note.status IS 'Note lifecycle status: ACTIVE, ARCHIVED, DELETED';
COMMENT ON COLUMN note.author_id IS 'UUID of the user who created the note';
COMMENT ON COLUMN note.project_id IS 'UUID of linked project (required when visibility = PROJECT)';
COMMENT ON COLUMN note.archived_at IS 'Timestamp when note was archived (NULL if not archived)';

-- =============================================================================
-- Table: comment
-- Description: Comments on notes (entity within Note aggregate)
-- =============================================================================
CREATE TABLE comment (
    comment_id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    note_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content VARCHAR(5000) NOT NULL,
    parent_comment_id UUID,
    solution_status VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comment_note
        FOREIGN KEY (note_id) REFERENCES note(note_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comment(comment_id) ON DELETE SET NULL,

    CONSTRAINT chk_solution_status CHECK (solution_status IN ('REGULAR', 'PROPOSED', 'VALIDATED')),
    CONSTRAINT chk_moderation_status CHECK (moderation_status IN ('VISIBLE', 'CENSORED', 'UNDER_REVIEW')),
    CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0)
);

CREATE INDEX idx_comment_note_id ON comment(note_id);
CREATE INDEX idx_comment_parent_id ON comment(parent_comment_id) WHERE parent_comment_id IS NOT NULL;
CREATE INDEX idx_comment_solution_status ON comment(solution_status) WHERE solution_status != 'REGULAR';
CREATE INDEX idx_comment_author_id ON comment(author_id);

COMMENT ON TABLE comment IS 'Comments and discussions on notes (within Note aggregate boundary)';
COMMENT ON COLUMN comment.note_id IS 'Parent note (CASCADE DELETE enforces aggregate boundary)';
COMMENT ON COLUMN comment.parent_comment_id IS 'Parent comment for threading (ON DELETE SET NULL)';
COMMENT ON COLUMN comment.solution_status IS 'Solution workflow: REGULAR, PROPOSED, or VALIDATED';
COMMENT ON COLUMN comment.moderation_status IS 'Moderation state: VISIBLE, CENSORED, or UNDER_REVIEW';

-- =============================================================================
-- Table: note_tag
-- Description: Many-to-many relationship between notes and tags
-- =============================================================================
CREATE TABLE note_tag (
    note_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (note_id, tag_id),

    CONSTRAINT fk_note_tag_note
        FOREIGN KEY (note_id) REFERENCES note(note_id) ON DELETE CASCADE,
    CONSTRAINT fk_note_tag_tag
        FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE RESTRICT
);

CREATE INDEX idx_note_tag_tag_id ON note_tag(tag_id);
CREATE INDEX idx_note_tag_note_id ON note_tag(note_id);

COMMENT ON TABLE note_tag IS 'Join table for many-to-many relationship between notes and tags';
COMMENT ON COLUMN note_tag.created_at IS 'When the tag was added to the note';

-- =============================================================================
-- Table: note_recipient
-- Description: Users with whom notes are explicitly shared
-- =============================================================================
CREATE TABLE note_recipient (
    note_id UUID NOT NULL,
    user_id UUID NOT NULL,
    shared_at TIMESTAMP NOT NULL,

    PRIMARY KEY (note_id, user_id),

    CONSTRAINT fk_note_recipient_note
        FOREIGN KEY (note_id) REFERENCES note(note_id) ON DELETE CASCADE
);

CREATE INDEX idx_note_recipient_user_id ON note_recipient(user_id);
CREATE INDEX idx_note_recipient_note_id ON note_recipient(note_id);
CREATE INDEX idx_note_recipient_shared_at ON note_recipient(shared_at);

COMMENT ON TABLE note_recipient IS 'Recipients with whom notes are explicitly shared (within Note aggregate boundary)';
COMMENT ON COLUMN note_recipient.user_id IS 'UUID of user receiving access';
COMMENT ON COLUMN note_recipient.shared_at IS 'Timestamp when note was shared';

-- =============================================================================
-- Triggers for automatic updated_at maintenance
-- =============================================================================

-- Trigger function for tag
CREATE OR REPLACE FUNCTION update_tag_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_tag_updated_at
    BEFORE UPDATE ON tag
    FOR EACH ROW
    EXECUTE FUNCTION update_tag_updated_at();

-- Trigger function for note
CREATE OR REPLACE FUNCTION update_note_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_note_updated_at
    BEFORE UPDATE ON note
    FOR EACH ROW
    EXECUTE FUNCTION update_note_updated_at();

-- Trigger function for comment
CREATE OR REPLACE FUNCTION update_comment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_comment_updated_at
    BEFORE UPDATE ON comment
    FOR EACH ROW
    EXECUTE FUNCTION update_comment_updated_at();

COMMIT;
