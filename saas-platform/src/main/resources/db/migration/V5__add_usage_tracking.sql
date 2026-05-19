-- V5__add_usage_tracking.sql
CREATE TABLE usage_records (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               tenant_id   UUID NOT NULL REFERENCES tenants(id),
                               record_date DATE NOT NULL,
                               endpoint    VARCHAR(255),
                               method      VARCHAR(10),
                               request_count BIGINT NOT NULL DEFAULT 1,
                               UNIQUE (tenant_id, record_date, endpoint, method)
);

CREATE INDEX idx_usage_tenant_date
    ON usage_records(tenant_id, record_date DESC);