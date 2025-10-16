-- Add password_hash column to users table for JWT authentication
ALTER TABLE users ADD COLUMN password_hash VARCHAR(60) NOT NULL DEFAULT '$2a$10$dummyHashForMigrationOnlyNotValidPassword';

-- Remove default after adding the column (for new inserts, password must be provided)
ALTER TABLE users ALTER COLUMN password_hash DROP DEFAULT;
