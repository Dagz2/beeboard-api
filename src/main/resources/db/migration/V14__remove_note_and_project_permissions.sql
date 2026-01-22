BEGIN;

-- ========================================
-- REMOVE NOTE AND PROJECT BC PERMISSIONS
-- ========================================
-- This migration removes all permissions related to Note BC and Project BC,
-- keeping only Access BC permissions.
-- Total permissions to remove: 23 (8 Note + 7 Project + 8 Task)

-- ====================================
-- STEP 1: REMOVE ROLE-PERMISSION ASSOCIATIONS
-- ====================================

-- Remove all role-permission associations for Note permissions
DELETE FROM role_permission
WHERE permission_id IN (
    SELECT permission_id FROM permission
    WHERE code IN (
        'NOTE_CREATE',
        'NOTE_UPDATE_OWN',
        'NOTE_UPDATE_ALL',
        'NOTE_DELETE_OWN',
        'NOTE_DELETE_ALL',
        'NOTE_VIEW_ALL',
        'NOTE_VIEW_PROJECT',
        'NOTE_COMMENT'
    )
);

-- Remove all role-permission associations for Project permissions
DELETE FROM role_permission
WHERE permission_id IN (
    SELECT permission_id FROM permission
    WHERE code IN (
        'PROJECT_CREATE',
        'PROJECT_UPDATE_ALL',
        'PROJECT_UPDATE_OWN',
        'PROJECT_DELETE',
        'PROJECT_VIEW_ALL',
        'PROJECT_VIEW_ASSIGNED',
        'PROJECT_ASSIGN_MEMBERS'
    )
);

-- Remove all role-permission associations for Task permissions
DELETE FROM role_permission
WHERE permission_id IN (
    SELECT permission_id FROM permission
    WHERE code IN (
        'TASK_CREATE',
        'TASK_UPDATE_OWN',
        'TASK_UPDATE_ALL',
        'TASK_DELETE_OWN',
        'TASK_DELETE_ALL',
        'TASK_ASSIGN',
        'TASK_VIEW_ALL',
        'TASK_VIEW_ASSIGNED'
    )
);

-- ====================================
-- STEP 2: DELETE PERMISSIONS FROM PERMISSION TABLE
-- ====================================

-- Delete Note BC permissions
DELETE FROM permission
WHERE code IN (
    'NOTE_CREATE',
    'NOTE_UPDATE_OWN',
    'NOTE_UPDATE_ALL',
    'NOTE_DELETE_OWN',
    'NOTE_DELETE_ALL',
    'NOTE_VIEW_ALL',
    'NOTE_VIEW_PROJECT',
    'NOTE_COMMENT'
);

-- Delete Project BC permissions
DELETE FROM permission
WHERE code IN (
    'PROJECT_CREATE',
    'PROJECT_UPDATE_ALL',
    'PROJECT_UPDATE_OWN',
    'PROJECT_DELETE',
    'PROJECT_VIEW_ALL',
    'PROJECT_VIEW_ASSIGNED',
    'PROJECT_ASSIGN_MEMBERS'
);

-- Delete Task permissions (part of Project BC)
DELETE FROM permission
WHERE code IN (
    'TASK_CREATE',
    'TASK_UPDATE_OWN',
    'TASK_UPDATE_ALL',
    'TASK_DELETE_OWN',
    'TASK_DELETE_ALL',
    'TASK_ASSIGN',
    'TASK_VIEW_ALL',
    'TASK_VIEW_ASSIGNED'
);

COMMIT;
END;
