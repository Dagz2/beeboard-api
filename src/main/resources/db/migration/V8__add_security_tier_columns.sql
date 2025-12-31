BEGIN;

-- Migration script to add security tier classification to permissions and roles
-- This implements the Tiered Administration security model

-- ====================================
-- ADD COLUMNS TO PERMISSION TABLE
-- ====================================

-- Add tier column with default value TIER_2 (Standard)
ALTER TABLE permission
ADD COLUMN tier VARCHAR(10) NOT NULL DEFAULT 'TIER_2';

-- Add is_system column with default value false
ALTER TABLE permission
ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT false;

-- ====================================
-- ADD COLUMNS TO ROLE TABLE
-- ====================================

-- Add tier column with default value TIER_2 (Standard)
ALTER TABLE role
ADD COLUMN tier VARCHAR(10) NOT NULL DEFAULT 'TIER_2';

-- Add is_system column with default value false
ALTER TABLE role
ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT false;

-- ====================================
-- UPDATE EXISTING PERMISSIONS WITH TIERS
-- ====================================

-- TIER_0 (Critical) - System administration permissions
UPDATE permission SET tier = 'TIER_0', is_system = true
WHERE code IN ('SYSTEM_ADMIN', 'AUDIT_VIEW');

-- TIER_1 (Sensitive) - User and role management
UPDATE permission SET tier = 'TIER_1', is_system = true
WHERE code IN ('USER_CREATE', 'USER_UPDATE', 'USER_DELETE', 'USER_LOCK', 'HARD_DELETE');

-- TIER_2 (Standard) - Already set by default for remaining permissions

-- ====================================
-- UPDATE EXISTING ROLES WITH TIERS
-- ====================================

-- Mark DIRECTOR as TIER_1 system role
UPDATE role SET tier = 'TIER_1', is_system = true
WHERE code = 'DIRECTOR';

-- Mark HR_MANAGER as TIER_2 system role
UPDATE role SET tier = 'TIER_2', is_system = true
WHERE code = 'HR_MANAGER';

-- Other roles remain TIER_2, non-system (default values)

COMMIT;

END;
