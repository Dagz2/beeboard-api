BEGIN;

-- ====================================
-- INSERTING PERMISSIONS
-- ====================================

-- User Management Permissions
INSERT INTO permission (code, name, description) VALUES
('USER_CREATE', 'Create User', 'Allows creating new user accounts'),
('USER_UPDATE', 'Edit User', 'Allows editing user information'),
('USER_DELETE', 'Delete User', 'Allows deleting user accounts'),
('USER_VIEW_ALL', 'View All Users', 'Allows viewing all user profiles'),
('USER_VIEW_TEAM', 'View Team', 'Allows viewing members of the project team'),
('USER_LOCK', 'Lock Account', 'Allows locking/unlocking a user account');

-- Project Management Permissions
INSERT INTO permission (code, name, description) VALUES
('PROJECT_CREATE', 'Create Project', 'Allows creating new projects'),
('PROJECT_UPDATE_ALL', 'Edit All Projects', 'Allows editing all projects'),
('PROJECT_UPDATE_OWN', 'Edit Own Projects', 'Allows editing one’s own projects'),
('PROJECT_DELETE', 'Delete Project', 'Allows deleting projects'),
('PROJECT_VIEW_ALL', 'View All Projects', 'Allows viewing all projects'),
('PROJECT_VIEW_ASSIGNED', 'View Assigned Projects', 'Allows viewing only assigned projects'),
('PROJECT_ASSIGN_MEMBERS', 'Assign Members', 'Allows assigning members to a project');

-- Note Management Permissions
INSERT INTO permission (code, name, description) VALUES
('NOTE_CREATE', 'Create Note', 'Allows creating notes'),
('NOTE_UPDATE_OWN', 'Edit Own Notes', 'Allows editing one’s own notes'),
('NOTE_UPDATE_ALL', 'Edit All Notes', 'Allows editing all notes'),
('NOTE_DELETE_OWN', 'Delete Own Notes', 'Allows deleting one’s own notes'),
('NOTE_DELETE_ALL', 'Delete All Notes', 'Allows deleting all notes'),
('NOTE_VIEW_ALL', 'View All Notes', 'Allows viewing all notes'),
('NOTE_VIEW_PROJECT', 'View Project Notes', 'Allows viewing notes related to assigned projects'),
('NOTE_COMMENT', 'Comment Notes', 'Allows commenting on notes');

-- Task Management Permissions
INSERT INTO permission (code, name, description) VALUES
('TASK_CREATE', 'Create Task', 'Allows creating tasks'),
('TASK_UPDATE_OWN', 'Edit Own Tasks', 'Allows editing one’s own tasks'),
('TASK_UPDATE_ALL', 'Edit All Tasks', 'Allows editing all tasks'),
('TASK_DELETE_OWN', 'Delete Own Tasks', 'Allows deleting one’s own tasks'),
('TASK_DELETE_ALL', 'Delete All Tasks', 'Allows deleting all tasks'),
('TASK_ASSIGN', 'Assign Tasks', 'Allows assigning tasks to users'),
('TASK_VIEW_ALL', 'View All Tasks', 'Allows viewing all tasks'),
('TASK_VIEW_ASSIGNED', 'View Assigned Tasks', 'Allows viewing assigned tasks');

-- Report Permissions
INSERT INTO permission (code, name, description) VALUES
('REPORT_VIEW_ALL', 'View All Reports', 'Allows viewing all reports'),
('REPORT_VIEW_OWN', 'View Own Reports', 'Allows viewing reports related to own projects'),
('REPORT_GENERATE', 'Generate Reports', 'Allows generating reports');

-- Administration Permissions
INSERT INTO permission (code, name, description) VALUES
('SYSTEM_ADMIN', 'System Administration', 'Full administrator access'),
('AUDIT_VIEW', 'View Audit Logs', 'Allows viewing audit logs'),
('HARD_DELETE', 'Permanent Deletion', 'Allows permanently deleting data');

-- ====================================
-- INSERTING ROLES
-- ====================================

INSERT INTO role (code, name, created_at, updated_at) VALUES
('DIRECTOR', 'Director', NOW(), NOW()),
('HR_MANAGER', 'HR Manager', NOW(), NOW()),
('PROJECT_MANAGER', 'Project Manager', NOW(), NOW()),
('TECH_LEAD', 'Technical Lead', NOW(), NOW()),
('SENIOR_DEVELOPER', 'Senior Developer', NOW(), NOW()),
('DEVELOPER', 'Developer', NOW(), NOW()),
('INTERN', 'Intern/Apprentice', NOW(), NOW()),
('EXTERNAL', 'External Contributor', NOW(), NOW());

-- ====================================
-- ROLE-PERMISSION ASSOCIATION
-- ====================================

-- DIRECTOR (Global vision + supervision)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'DIRECTOR'
AND p.code IN (
    'USER_VIEW_ALL', 'USER_LOCK',
    'PROJECT_CREATE', 'PROJECT_VIEW_ALL', 'PROJECT_ASSIGN_MEMBERS',
    'NOTE_VIEW_ALL',
    'TASK_VIEW_ALL',
    'REPORT_VIEW_ALL', 'REPORT_GENERATE',
    'AUDIT_VIEW', 'HARD_DELETE'
);

-- HR_MANAGER (User management)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'HR_MANAGER'
AND p.code IN (
    'USER_CREATE', 'USER_UPDATE', 'USER_DELETE', 'USER_VIEW_ALL', 'USER_LOCK',
    'REPORT_GENERATE'
);

-- PROJECT_MANAGER (Project management)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'PROJECT_MANAGER'
AND p.code IN (
    'USER_VIEW_ALL', 'USER_VIEW_TEAM',
    'PROJECT_CREATE', 'PROJECT_UPDATE_OWN', 'PROJECT_VIEW_ALL', 'PROJECT_ASSIGN_MEMBERS',
    'NOTE_CREATE', 'NOTE_UPDATE_OWN', 'NOTE_VIEW_PROJECT', 'NOTE_COMMENT',
    'TASK_CREATE', 'TASK_UPDATE_ALL', 'TASK_ASSIGN', 'TASK_VIEW_ALL',
    'REPORT_VIEW_OWN', 'REPORT_GENERATE'
);

-- TECH_LEAD (Technical leadership)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'TECH_LEAD'
AND p.code IN (
    'USER_VIEW_TEAM',
    'PROJECT_VIEW_ASSIGNED',
    'NOTE_CREATE', 'NOTE_UPDATE_OWN', 'NOTE_DELETE_OWN', 'NOTE_VIEW_PROJECT', 'NOTE_COMMENT',
    'TASK_CREATE', 'TASK_UPDATE_OWN', 'TASK_ASSIGN', 'TASK_VIEW_ASSIGNED'
);

-- SENIOR_DEVELOPER (Experienced developer)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'SENIOR_DEVELOPER'
AND p.code IN (
    'USER_VIEW_TEAM',
    'PROJECT_VIEW_ASSIGNED',
    'NOTE_CREATE', 'NOTE_UPDATE_OWN', 'NOTE_DELETE_OWN', 'NOTE_VIEW_PROJECT', 'NOTE_COMMENT',
    'TASK_UPDATE_OWN', 'TASK_VIEW_ASSIGNED'
);

-- DEVELOPER (Standard developer)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'DEVELOPER'
AND p.code IN (
    'USER_VIEW_TEAM',
    'PROJECT_VIEW_ASSIGNED',
    'NOTE_CREATE', 'NOTE_UPDATE_OWN', 'NOTE_VIEW_PROJECT', 'NOTE_COMMENT',
    'TASK_UPDATE_OWN', 'TASK_VIEW_ASSIGNED'
);

-- INTERN (Intern/Apprentice - limited access)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'INTERN'
AND p.code IN (
    'PROJECT_VIEW_ASSIGNED',
    'NOTE_VIEW_PROJECT', 'NOTE_COMMENT',
    'TASK_UPDATE_OWN', 'TASK_VIEW_ASSIGNED'
);

-- EXTERNAL (External contributor - minimal access)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM role r, permission p
WHERE r.code = 'EXTERNAL'
AND p.code IN (
    'PROJECT_VIEW_ASSIGNED',
    'NOTE_VIEW_PROJECT',
    'TASK_VIEW_ASSIGNED'
);

COMMIT;

END;