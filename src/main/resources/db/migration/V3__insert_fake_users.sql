-- ============================================
-- Mock User Profiles Insert Script
-- Based on The Office and Community characters
-- ============================================

BEGIN;

-- ============================================
-- USER PROFILES INSERTION
-- Distribution represents a realistic company org chart
-- ============================================

-- DIRECTOR (1 profile)
-- Michael Scott - Regional Manager becomes Director
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Michael', 
    'Scott',
    'michael.scott@memory-dev.com',
    '2030-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '2 hours',
    role_id,
    NOW() - INTERVAL '5 years',
    NOW()
FROM role WHERE code = 'DIRECTOR';

-- HR_MANAGER (2 profiles)
-- Toby Flenderson - HR Representative
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Toby', 
    'Flenderson',
    'toby.flenderson@memory-dev.com',
    '2028-06-30'::DATE,
    FALSE,
    NOW() - INTERVAL '1 day',
    role_id,
    NOW() - INTERVAL '4 years',
    NOW()
FROM role WHERE code = 'HR_MANAGER';

-- Shirley Bennett - Community's maternal figure fits HR
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Shirley', 
    'Bennett',
    'shirley.bennett@memory-dev.com',
    '2029-03-31'::DATE,
    FALSE,
    NOW() - INTERVAL '4 hours',
    role_id,
    NOW() - INTERVAL '3 years',
    NOW()
FROM role WHERE code = 'HR_MANAGER';

-- PROJECT_MANAGER (3 profiles)
-- Jim Halpert - Natural leader
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Jim', 
    'Halpert',
    'jim.halpert@memory-dev.com',
    '2029-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '30 minutes',
    role_id,
    NOW() - INTERVAL '3 years 6 months',
    NOW()
FROM role WHERE code = 'PROJECT_MANAGER';

-- Jeff Winger - Natural leader from Community
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Jeff', 
    'Winger',
    'jeff.winger@memory-dev.com',
    '2028-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '1 hour',
    role_id,
    NOW() - INTERVAL '2 years 8 months',
    NOW()
FROM role WHERE code = 'PROJECT_MANAGER';

-- David Wallace - CFO level, managing strategic projects
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'David', 
    'Wallace',
    'david.wallace@memory-dev.com',
    '2031-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '3 hours',
    role_id,
    NOW() - INTERVAL '4 years',
    NOW()
FROM role WHERE code = 'PROJECT_MANAGER';

-- TECH_LEAD (2 profiles)
-- Dwight Schrute - Assistant (to the) Regional Manager
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Dwight', 
    'Schrute',
    'dwight.schrute@memory-dev.com',
    '2030-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '10 minutes',
    role_id,
    NOW() - INTERVAL '5 years',
    NOW()
FROM role WHERE code = 'TECH_LEAD';

-- Abed Nadir - Tech genius from Community
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Abed', 
    'Nadir',
    'abed.nadir@memory-dev.com',
    '2029-09-30'::DATE,
    FALSE,
    NOW() - INTERVAL '45 minutes',
    role_id,
    NOW() - INTERVAL '2 years 3 months',
    NOW()
FROM role WHERE code = 'TECH_LEAD';

-- SENIOR_DEVELOPER (4 profiles)
-- Oscar Martinez - Accounting but meticulous like a senior dev
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Oscar', 
    'Martinez',
    'oscar.martinez@memory-dev.com',
    '2029-06-30'::DATE,
    FALSE,
    NOW() - INTERVAL '2 hours',
    role_id,
    NOW() - INTERVAL '4 years 2 months',
    NOW()
FROM role WHERE code = 'SENIOR_DEVELOPER';

-- Angela Martin - Detail-oriented perfectionist
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Angela', 
    'Martin',
    'angela.martin@memory-dev.com',
    '2028-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '5 hours',
    role_id,
    NOW() - INTERVAL '4 years 6 months',
    NOW()
FROM role WHERE code = 'SENIOR_DEVELOPER';

-- Troy Barnes - Talented and dedicated
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Troy', 
    'Barnes',
    'troy.barnes@memory-dev.com',
    '2029-03-31'::DATE,
    FALSE,
    NOW() - INTERVAL '20 minutes',
    role_id,
    NOW() - INTERVAL '3 years',
    NOW()
FROM role WHERE code = 'SENIOR_DEVELOPER';

-- Britta Perry - Experienced and principled
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Britta', 
    'Perry',
    'britta.perry@memory-dev.com',
    '2028-09-30'::DATE,
    FALSE,
    NOW() - INTERVAL '1 hour 30 minutes',
    role_id,
    NOW() - INTERVAL '3 years 4 months',
    NOW()
FROM role WHERE code = 'SENIOR_DEVELOPER';

-- DEVELOPER (5 profiles)
-- Pam Beesly - Receptionist turned developer
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Pam', 
    'Beesly',
    'pam.beesly@memory-dev.com',
    '2029-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '15 minutes',
    role_id,
    NOW() - INTERVAL '2 years',
    NOW()
FROM role WHERE code = 'DEVELOPER';

-- Stanley Hudson - Steady and reliable
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Stanley', 
    'Hudson',
    'stanley.hudson@memory-dev.com',
    '2027-02-28'::DATE,  -- Closer to retirement
    FALSE,
    NOW() - INTERVAL '6 hours',
    role_id,
    NOW() - INTERVAL '8 years',
    NOW()
FROM role WHERE code = 'DEVELOPER';

-- Phyllis Vance - Experienced team member
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Phyllis', 
    'Vance',
    'phyllis.vance@memory-dev.com',
    '2028-06-30'::DATE,
    FALSE,
    NOW() - INTERVAL '3 hours',
    role_id,
    NOW() - INTERVAL '6 years',
    NOW()
FROM role WHERE code = 'DEVELOPER';

-- Kevin Malone - Trying his best
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Kevin', 
    'Malone',
    'kevin.malone@memory-dev.com',
    '2028-12-31'::DATE,
    FALSE,
    NOW() - INTERVAL '7 hours',
    role_id,
    NOW() - INTERVAL '5 years',
    NOW()
FROM role WHERE code = 'DEVELOPER';

-- Dean Pelton - Multitasker from Community
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Craig', 
    'Pelton',
    'craig.pelton@memory-dev.com',
    '2029-06-30'::DATE,
    FALSE,
    NOW() - INTERVAL '40 minutes',
    role_id,
    NOW() - INTERVAL '1 year 6 months',
    NOW()
FROM role WHERE code = 'DEVELOPER';

-- INTERN (3 profiles)
-- Ryan Howard - Temp turned intern
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Ryan', 
    'Howard',
    'ryan.howard@memory-dev.com',
    '2025-06-30'::DATE,  -- Short-term contract
    FALSE,
    NOW() - INTERVAL '25 minutes',
    role_id,
    NOW() - INTERVAL '4 months',
    NOW()
FROM role WHERE code = 'INTERN';

-- Annie Edison - Overachiever student
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Annie', 
    'Edison',
    'annie.edison@memory-dev.com',
    '2025-08-31'::DATE,  -- Summer internship
    FALSE,
    NOW() - INTERVAL '5 minutes',
    role_id,
    NOW() - INTERVAL '2 months',
    NOW()
FROM role WHERE code = 'INTERN';

-- Erin Hannon - New receptionist/intern
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Erin', 
    'Hannon',
    'erin.hannon@memory-dev.com',
    '2025-12-31'::DATE,  -- Year-long internship
    FALSE,
    NOW() - INTERVAL '1 hour',
    role_id,
    NOW() - INTERVAL '1 month',
    NOW()
FROM role WHERE code = 'INTERN';

-- EXTERNAL (2 profiles)
-- Creed Bratton - Nobody knows what he does
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Creed', 
    'Bratton',
    'creed.bratton@memory-dev.com',
    '2025-03-31'::DATE,  -- Short-term consultant
    FALSE,
    NOW() - INTERVAL '3 days',
    role_id,
    NOW() - INTERVAL '2 weeks',
    NOW()
FROM role WHERE code = 'EXTERNAL';

-- Ben Chang - External consultant from Community
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Ben', 
    'Chang',
    'ben.chang@memory-dev.com',
    '2025-02-28'::DATE,  -- Very short contract
    FALSE,
    NOW() - INTERVAL '2 days',
    role_id,
    NOW() - INTERVAL '1 month',
    NOW()
FROM role WHERE code = 'EXTERNAL';

-- One locked account example (fired/suspended employee)
-- Pierce Hawthorne - Problematic, account locked
INSERT INTO users (first_name, last_name, email, contract_end_date, is_locked, last_activity_date, role_id, created_at, updated_at)
SELECT 
    'Pierce', 
    'Hawthorne',
    'pierce.hawthorne@memory-dev.com',
    '2024-12-31'::DATE,  -- Contract ended
    TRUE,  -- Account locked
    NOW() - INTERVAL '30 days',
    role_id,
    NOW() - INTERVAL '2 years',
    NOW() - INTERVAL '30 days'
FROM role WHERE code = 'DEVELOPER';

COMMIT;
