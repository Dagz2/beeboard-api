BEGIN;

-- =============================================================================
-- Migration: V17__populate_author_view.sql
-- Description: Initial population of author_view from existing users
-- Purpose: Populate CQRS read model with existing user data
-- =============================================================================

-- =============================================================================
-- Insert all users into author_view with proper translations
-- =============================================================================

INSERT INTO author_view (user_id, access_level, account_status, full_name, created_at, updated_at)
SELECT
    u.user_id,

    -- ACL Translation: ApplicationRole → AccessLevel (Note BC vocabulary)
    CASE r.application_role
        WHEN 'ADMIN' THEN 'SUPERVISOR'
        WHEN 'SUPERVISOR' THEN 'SUPERVISOR'
        WHEN 'MANAGER' THEN 'MODERATOR'
        WHEN 'MEMBER' THEN 'COMMENTER'
        WHEN 'GUEST' THEN 'VIEWER'
        ELSE 'VIEWER'  -- Default fallback (should never happen with constraints)
    END AS access_level,

    -- Account Status Derivation (matching User.getAccountStatus() logic)
    -- Priority: LOCKED > EXPIRED > ACTIVE
    CASE
        WHEN u.is_locked = true THEN 'LOCKED'
        WHEN u.contract_end_date < CURRENT_DATE THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END AS account_status,

    -- Full name concatenation
    u.first_name || ' ' || u.last_name AS full_name,

    -- Timestamps
    NOW() AS created_at,
    NOW() AS updated_at
FROM users u
INNER JOIN role r ON u.role_id = r.role_id
WHERE NOT EXISTS (
    -- Idempotency: Skip if user already exists in author_view
    SELECT 1 FROM author_view av WHERE av.user_id = u.user_id
);

-- =============================================================================
-- Log statistics
-- =============================================================================

DO $$
DECLARE
    rows_inserted INTEGER;
BEGIN
    GET DIAGNOSTICS rows_inserted = ROW_COUNT;
    RAISE NOTICE 'Populated author_view with % users', rows_inserted;
END $$;

COMMIT;
