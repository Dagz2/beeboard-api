BEGIN;

-- Migration script to create SUPER_ADMIN role with TIER_0 permissions
-- This implements the Tiered Administration security model

-- ====================================
-- CREATE TIER_0 PERMISSIONS
-- ====================================

INSERT INTO permission (code, name, description, tier, is_system) VALUES
('MANAGE_PERMISSIONS', 'Manage Permissions', 'Create, update, and delete system permissions', 'TIER_0', true),
('MANAGE_SYSTEM_ROLES', 'Manage System Roles', 'Modify system-protected roles', 'TIER_0', true),
('VIEW_AUDIT_LOGS', 'View Audit Logs', 'Access to view all audit trail logs', 'TIER_0', true),
('ASSIGN_CRITICAL_ROLES', 'Assign Critical Roles', 'Assign TIER_0 and TIER_1 roles to users', 'TIER_0', true);

-- ====================================
-- CREATE TIER_1 PERMISSIONS
-- ====================================

INSERT INTO permission (code, name, description, tier, is_system) VALUES
('MANAGE_ROLES', 'Manage Roles', 'Create and modify non-system roles', 'TIER_1', true),
('ASSIGN_STANDARD_ROLES', 'Assign Standard Roles', 'Assign TIER_2 roles to users', 'TIER_1', true);

-- ====================================
-- CREATE TIER_2 PERMISSIONS
-- ====================================

INSERT INTO permission (code, name, description, tier, is_system) VALUES
('ASSIGN_BASIC_ROLES', 'Assign Basic Roles', 'Assign basic TIER_2 roles to new users', 'TIER_2', true);

-- ====================================
-- CREATE SUPER_ADMIN ROLE
-- ====================================

INSERT INTO role (code, name, tier, is_system, created_at, updated_at) VALUES
('SUPER_ADMIN', 'Super Administrator', 'TIER_0', true, NOW(), NOW());

-- ====================================
-- ASSIGN ALL TIER_0 PERMISSIONS TO SUPER_ADMIN
-- ====================================

INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'SUPER_ADMIN'
AND p.tier = 'TIER_0';

-- Also grant all other permissions to SUPER_ADMIN (full access)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'SUPER_ADMIN'
AND p.tier != 'TIER_0';

-- ====================================
-- GRANT NEW TIER_1 PERMISSIONS TO DIRECTOR
-- ====================================

INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'DIRECTOR'
AND p.code IN ('MANAGE_ROLES', 'ASSIGN_STANDARD_ROLES');

-- ====================================
-- GRANT NEW TIER_2 PERMISSION TO HR_MANAGER
-- ====================================

INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'HR_MANAGER'
AND p.code = 'ASSIGN_BASIC_ROLES';

-- ====================================
-- CREATE SUPER_ADMIN USER
-- ====================================

INSERT INTO users (
    first_name,
    last_name,
    email,
    password_hash,
    contract_end_date,
    is_locked,
    last_activity_date,
    role_id,
    created_at,
    updated_at
)
SELECT
    'Super',
    'Administrator',
    't.derou@proton.me',
    '$2a$10$iTDUGQK6MyCJhC2wSem.8e5Nt3rf4L2lLDABjCEC20iLsENuaiFVC',
    '2099-12-31',
    false,
    NOW(),
    r.role_id,
    NOW(),
    NOW()
FROM role r
WHERE r.code = 'SUPER_ADMIN';

COMMIT;

END;
