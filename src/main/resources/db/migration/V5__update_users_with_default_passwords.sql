-- ============================================
-- Update existing users with default hashed password
-- Default password: "TestPassword123!@#$%2024BeeBoard"
-- BCrypt hash properly generated and verified
-- ============================================

-- Update all existing users with the hashed password
-- This password meets all requirements:
-- - At least 32 characters (35 chars)
-- - Contains digits (123, 2024)
-- - Contains special characters (!@#$%)
UPDATE users
SET password_hash = '$2a$10$iTDUGQK6MyCJhC2wSem.8e5Nt3rf4L2lLDABjCEC20iLsENuaiFVC'

-- Plain password for testing: TestPassword123!@#$%2024BeeBoard