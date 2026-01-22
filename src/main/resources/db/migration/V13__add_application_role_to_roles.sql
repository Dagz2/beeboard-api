BEGIN;

-- ====================================
-- ADD APPLICATION_ROLE COLUMN
-- ====================================

ALTER TABLE role
ADD COLUMN application_role VARCHAR(20);

-- ====================================
-- POPULATE EXISTING ROLES
-- ====================================

-- ADMIN: SUPER_ADMIN
UPDATE role SET application_role = 'ADMIN' WHERE code = 'SUPER_ADMIN';

-- SUPERVISOR: DIRECTOR
UPDATE role SET application_role = 'SUPERVISOR' WHERE code = 'DIRECTOR';

-- MANAGER: PROJECT_MANAGER, TECH_LEAD, HR_MANAGER
UPDATE role SET application_role = 'MANAGER' WHERE code IN ('PROJECT_MANAGER', 'TECH_LEAD', 'HR_MANAGER');

-- MEMBER: DEVELOPER, SENIOR_DEVELOPER
UPDATE role SET application_role = 'MEMBER' WHERE code IN ('DEVELOPER', 'SENIOR_DEVELOPER');

-- GUEST: INTERN, EXTERNAL
UPDATE role SET application_role = 'GUEST' WHERE code IN ('INTERN', 'EXTERNAL');

-- Set default for any unmapped roles (custom roles created after V2)
UPDATE role SET application_role = 'MEMBER' WHERE application_role IS NULL;

-- ====================================
-- MAKE COLUMN NOT NULL
-- ====================================

ALTER TABLE role
ALTER COLUMN application_role SET NOT NULL;

-- ====================================
-- ADD CONSTRAINT
-- ====================================

ALTER TABLE role
ADD CONSTRAINT chk_application_role
CHECK (application_role IN ('ADMIN', 'SUPERVISOR', 'MANAGER', 'MEMBER', 'GUEST'));

-- ====================================
-- ADD INDEX FOR QUERIES
-- ====================================

CREATE INDEX idx_role_application_role ON role(application_role);

COMMIT;
END;