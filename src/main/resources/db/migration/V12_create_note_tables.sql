BEGIN;
-- Migration script to create Note BC tables
-- This creates the tag and note tables for the Note Management bounded context

-- =============================================================================
-- Table: tag
-- Description: Stores tags used to classify notes
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

-- Index for performance on name lookups (case-insensitive)
CREATE INDEX idx_tag_name ON tag(name);

-- Index for finding unused tags
CREATE INDEX idx_tag_usage_count ON tag(usage_count);

COMMENT ON TABLE tag IS 'Tags for flexible classification of notes';
COMMENT ON COLUMN tag.name IS 'Unique tag name (lowercase, alphanumeric with hyphens)';
COMMENT ON COLUMN tag.usage_count IS 'Number of notes using this tag';

-- =============================================================================
-- Table: note
-- Description: Main table for notes (aggregate root)
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

    -- JSONB columns for embedded collections (aggregate pattern)
    tag_ids JSONB NOT NULL DEFAULT '[]'::jsonb,
    recipients JSONB NOT NULL DEFAULT '[]'::jsonb,
    comments JSONB NOT NULL DEFAULT '[]'::jsonb,

    -- Audit and lifecycle
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    archived_at TIMESTAMP,

    -- Constraints
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

-- =============================================================================
-- Indexes for Performance
-- =============================================================================

-- Index for author queries (most common)
CREATE INDEX idx_note_author_id ON note(author_id);

-- Index for category filtering
CREATE INDEX idx_note_category ON note(category);

-- Index for visibility filtering
CREATE INDEX idx_note_visibility ON note(visibility);

-- Index for status filtering
CREATE INDEX idx_note_status ON note(status);

-- Index for project filtering
CREATE INDEX idx_note_project_id ON note(project_id) WHERE project_id IS NOT NULL;

-- Composite index for active notes by author (very common query)
CREATE INDEX idx_note_author_active ON note(author_id, status) WHERE status = 'ACTIVE';

-- Index for archived notes cleanup
CREATE INDEX idx_note_archived_at ON note(archived_at) WHERE archived_at IS NOT NULL;

-- GIN indexes for JSONB columns (enables efficient querying)
CREATE INDEX idx_note_tag_ids ON note USING GIN (tag_ids);
CREATE INDEX idx_note_recipients ON note USING GIN (recipients);
CREATE INDEX idx_note_comments ON note USING GIN (comments);

-- Full-text search index on title and content
CREATE INDEX idx_note_fulltext ON note USING GIN (
    to_tsvector('english', title || ' ' || content)
);

-- =============================================================================
-- Comments for Documentation
-- =============================================================================

COMMENT ON TABLE note IS 'Notes for knowledge sharing and discussion (Note BC aggregate root)';
COMMENT ON COLUMN note.title IS 'Note title (max 200 characters)';
COMMENT ON COLUMN note.content IS 'Note body content (max 50,000 characters)';
COMMENT ON COLUMN note.content_type IS 'Type of content: TEXT, CODE, IMAGE, or MIXED';
COMMENT ON COLUMN note.category IS 'Mandatory category: BUG, TIP, DOCUMENTATION, QUESTION, ANNOUNCEMENT, RECOMMENDATION, BUSINESS_RULE, CODE_SNIPPET';
COMMENT ON COLUMN note.visibility IS 'Who can see this note: PRIVATE, SHARED, PROJECT';
COMMENT ON COLUMN note.status IS 'Note lifecycle status: ACTIVE, ARCHIVED, DELETED';
COMMENT ON COLUMN note.author_id IS 'UUID of the user who created the note';
COMMENT ON COLUMN note.project_id IS 'UUID of linked project (required when visibility = PROJECT)';
COMMENT ON COLUMN note.tag_ids IS 'JSONB array of tag UUIDs attached to this note';
COMMENT ON COLUMN note.recipients IS 'JSONB array of recipients (users with whom note is explicitly shared)';
COMMENT ON COLUMN note.comments IS 'JSONB array of comments forming the discussion';
COMMENT ON COLUMN note.archived_at IS 'Timestamp when note was archived (NULL if not archived)';

-- =============================================================================
-- Update Trigger for updated_at
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_note_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for note table
CREATE TRIGGER trigger_note_updated_at
    BEFORE UPDATE ON note
    FOR EACH ROW
    EXECUTE FUNCTION update_note_updated_at();

-- Trigger for tag table
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

COMMIT;
