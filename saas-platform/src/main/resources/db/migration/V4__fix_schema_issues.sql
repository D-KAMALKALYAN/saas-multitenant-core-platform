-- V4__fix_schema_issues.sql

-- Remove leftover scope array column from api_keys
ALTER TABLE api_keys DROP COLUMN IF EXISTS scope;

-- Make audit_logs.tenant_id nullable for auth events
ALTER TABLE audit_logs ALTER COLUMN tenant_id DROP NOT NULL;

-- Drop old role constraint safely
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;

-- Add updated role constraint
ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'MEMBER', 'VIEWER'));