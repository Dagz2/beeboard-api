BEGIN;

-- Migration script to create immutable audit log table
-- This implements the audit trail security requirement

-- ====================================
-- CREATE AUDIT LOG TABLE
-- ====================================

CREATE TABLE audit_log (
    id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    actor_id UUID NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    actor_tier VARCHAR(10) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- ====================================
-- CREATE INDEXES FOR COMMON QUERIES
-- ====================================

-- Index for querying by actor
CREATE INDEX idx_audit_log_actor_id ON audit_log(actor_id);

-- Index for querying by action type
CREATE INDEX idx_audit_log_action ON audit_log(action);

-- Index for querying by time range
CREATE INDEX idx_audit_log_occurred_at ON audit_log(occurred_at);

-- Index for querying by target
CREATE INDEX idx_audit_log_target ON audit_log(target_type, target_id);

-- ====================================
-- CREATE IMMUTABILITY TRIGGER
-- ====================================

-- Function to prevent UPDATE and DELETE operations on audit_log
CREATE OR REPLACE FUNCTION prevent_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs cannot be modified or deleted. This is a security requirement.';
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger to enforce immutability on UPDATE
CREATE TRIGGER audit_log_prevent_update
BEFORE UPDATE ON audit_log
FOR EACH ROW EXECUTE FUNCTION prevent_audit_modification();

-- Trigger to enforce immutability on DELETE
CREATE TRIGGER audit_log_prevent_delete
BEFORE DELETE ON audit_log
FOR EACH ROW EXECUTE FUNCTION prevent_audit_modification();

-- ====================================
-- COMMENTS
-- ====================================

COMMENT ON TABLE audit_log IS 'Immutable audit trail for security-sensitive operations';
COMMENT ON COLUMN audit_log.actor_id IS 'User ID of the actor who performed the action';
COMMENT ON COLUMN audit_log.actor_email IS 'Email of the actor at the time of the action';
COMMENT ON COLUMN audit_log.actor_tier IS 'Security tier of the actor (TIER_0, TIER_1, TIER_2)';
COMMENT ON COLUMN audit_log.action IS 'Type of action performed (CREATE_USER, ASSIGN_ROLE, etc.)';
COMMENT ON COLUMN audit_log.target_type IS 'Type of resource affected (USER, ROLE, PERMISSION)';
COMMENT ON COLUMN audit_log.target_id IS 'ID of the resource affected';
COMMENT ON COLUMN audit_log.old_value IS 'JSON snapshot of the resource before modification';
COMMENT ON COLUMN audit_log.new_value IS 'JSON snapshot of the resource after modification';
COMMENT ON COLUMN audit_log.ip_address IS 'IP address of the client making the request';
COMMENT ON COLUMN audit_log.user_agent IS 'User-Agent header of the client';

COMMIT;

END;
