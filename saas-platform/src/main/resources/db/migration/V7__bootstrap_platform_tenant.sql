-- V7__bootstrap_platform_tenant.sql

-- Insert platform tenant (idempotent)
INSERT INTO tenants (id, name, slug, email, plan, status, created_at, updated_at)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'Platform Administration',
           'platform',
           'platform@saas.internal',
           'ENTERPRISE',
           'ACTIVE',
           NOW(),
           NOW()
       ) ON CONFLICT (slug) DO NOTHING;