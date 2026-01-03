-- Add view permissions for roles and permissions management
-- These allow read-only access to system configuration

INSERT INTO permission (permission_id, code, name, description, tier, is_system)
VALUES
    (gen_random_uuid(), 'ROLE_VIEW', 'View Roles', 'Allows viewing of role definitions', 'TIER_2', false),
    (gen_random_uuid(), 'PERMISSION_VIEW', 'View Permissions', 'Allows viewing of permission definitions', 'TIER_1', false);

-- Grant ROLE_VIEW to DIRECTOR and SUPER_ADMIN
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code IN ('DIRECTOR', 'SUPER_ADMIN') AND p.code = 'ROLE_VIEW';

-- Grant PERMISSION_VIEW to DIRECTOR and SUPER_ADMIN
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code IN ('DIRECTOR', 'SUPER_ADMIN') AND p.code = 'PERMISSION_VIEW';

-- Grant USER_VIEW_ALL to DIRECTOR, SUPER_ADMIN and HR_MANAGER (if not already)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code IN ('DIRECTOR', 'SUPER_ADMIN', 'HR_MANAGER') AND p.code = 'USER_VIEW_ALL'
ON CONFLICT DO NOTHING;
