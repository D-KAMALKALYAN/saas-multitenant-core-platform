-- V1__baseline_schema.sql
-- Baseline migration representing current schema state
-- Generated from live database on 2026-05-10


-- =========================================
-- TENANTS
-- =========================================


CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    plan VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL
    );

-- UNIQUE CONSTRAINTS
ALTER TABLE tenants
    ADD CONSTRAINT uk_tenants_email
        UNIQUE (email);

ALTER TABLE tenants
    ADD CONSTRAINT uk_tenants_slug
        UNIQUE (slug);

-- CHECK CONSTRAINTS
ALTER TABLE tenants
    ADD CONSTRAINT chk_tenants_plan
        CHECK (
            plan IN ('BASIC', 'PRO', 'PREMIUM', 'ENTERPRISE')
            );


ALTER TABLE tenants
    ADD CONSTRAINT chk_tenants_status
        CHECK (
            status IN ('ACTIVE', 'INACTIVE')
            );

-- INDEXES
CREATE INDEX idx_tenants_slug
    ON tenants(slug);

CREATE INDEX idx_tenants_status
    ON tenants(status);



-- =========================================
-- USERS TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS users (

       id UUID PRIMARY KEY,

       tenant_id UUID NOT NULL,

       first_name VARCHAR(255) NOT NULL,

       last_name VARCHAR(255) NOT NULL,

       email VARCHAR(255) NOT NULL,

       password VARCHAR(255) NOT NULL,

       role VARCHAR(50) NOT NULL,

       status VARCHAR(50) NOT NULL,

       created_at TIMESTAMP,

       updated_at TIMESTAMP,

       deleted_at TIMESTAMP
);

-- =========================================
-- FOREIGN KEY CONSTRAINTS
-- =========================================

ALTER TABLE users
    ADD CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id);

-- =========================================
-- UNIQUE CONSTRAINTS
-- =========================================

ALTER TABLE users
    ADD CONSTRAINT uk_users_email_tenant
        UNIQUE (email, tenant_id);

-- =========================================
-- CHECK CONSTRAINTS
-- =========================================

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (
            role IN (
                     'MEMBER',
                     'ADMIN',
                     'VIEWER'
                )
            );

ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE'
                )
            );

-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_users_tenant_id
    ON users(tenant_id);




-- =========================================
-- REFRESH TOKENS TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS refresh_tokens (

            id UUID PRIMARY KEY,

            user_id UUID NOT NULL,

            tenant_id UUID NOT NULL,

            token VARCHAR(500) NOT NULL,

            expires_at TIMESTAMP NOT NULL,

            revoked BOOLEAN NOT NULL,

            created_at TIMESTAMP NOT NULL
);

-- =========================================
-- FOREIGN KEY CONSTRAINTS
-- =========================================

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users(id);

-- =========================================
-- UNIQUE CONSTRAINTS
-- =========================================

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uk_refresh_tokens_token
        UNIQUE (token);

-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_token
    ON refresh_tokens(token);




-- =========================================
-- AUDIT LOGS TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS audit_logs (

                id UUID PRIMARY KEY,

                tenant_id UUID NOT NULL,

                user_id UUID,

                user_email VARCHAR(255),

                action SMALLINT NOT NULL,

                entity_type VARCHAR(255),

                entity_id VARCHAR(255),

                status VARCHAR(100) NOT NULL,

                ip_address VARCHAR(100),

                user_agent VARCHAR(500),

                metadata JSONB,

                created_at TIMESTAMP NOT NULL
);

-- =========================================
-- CHECK CONSTRAINTS
-- =========================================

ALTER TABLE audit_logs
    ADD CONSTRAINT chk_audit_logs_action
        CHECK (
    action >= 0
    AND action <= 11
    );

-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_audit_tenant_created
    ON audit_logs(tenant_id, created_at);

CREATE INDEX idx_audit_user_id
    ON audit_logs(user_id, created_at);



-- =========================================
-- API KEYS TABLE
-- =========================================

CREATE TABLE IF NOT EXISTS api_keys (

                                        id UUID PRIMARY KEY,

                                        tenant_id UUID NOT NULL,

                                        created_by UUID NOT NULL,

                                        key_hash VARCHAR(255) NOT NULL,

    key_prefix VARCHAR(100),

    name VARCHAR(255),

    scopes VARCHAR(255),

    scope TEXT[],

    status VARCHAR(50),

    created_at TIMESTAMP,

    expires_at TIMESTAMP,

    revoked_at TIMESTAMP,

    last_used_at TIMESTAMP
    );

-- =========================================
-- FOREIGN KEY CONSTRAINTS
-- =========================================

ALTER TABLE api_keys
    ADD CONSTRAINT fk_api_keys_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenants(id);

-- =========================================
-- UNIQUE CONSTRAINTS
-- =========================================

ALTER TABLE api_keys
    ADD CONSTRAINT uk_api_keys_key_hash
        UNIQUE (key_hash);

-- =========================================
-- CHECK CONSTRAINTS
-- =========================================

ALTER TABLE api_keys
    ADD CONSTRAINT chk_api_keys_status
        CHECK (
            status IN ('ACTIVE', 'REVOKED')
            );

-- =========================================
-- INDEXES
-- =========================================

CREATE UNIQUE INDEX IF NOT EXISTS idx_api_keys_key_hash
    ON api_keys(key_hash);

CREATE INDEX IF NOT EXISTS idx_api_keys_tenant_id
    ON api_keys(tenant_id);

CREATE INDEX IF NOT EXISTS idx_api_keys_tenant_status
    ON api_keys(tenant_id, status);