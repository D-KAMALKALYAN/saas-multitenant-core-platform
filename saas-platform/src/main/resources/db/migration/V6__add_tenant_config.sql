-- V6__add_tenant_config.sql
CREATE TABLE IF NOT EXISTS tenant_configs (
                                id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                tenant_id   UUID NOT NULL REFERENCES tenants(id),
                                config_key  VARCHAR(100) NOT NULL,
                                config_value VARCHAR(500) NOT NULL,
                                created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                UNIQUE (tenant_id, config_key)
);

CREATE INDEX IF NOT EXISTS idx_tenant_configs_tenant_id
    ON tenant_configs(tenant_id);