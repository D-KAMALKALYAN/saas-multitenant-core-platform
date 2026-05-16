package com.saasplatform.common.monitoring;

import com.saasplatform.tenant.repository.TenantRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;


@Component
public class TenantHealthIndicator implements HealthIndicator {

    private final TenantRepository tenantRepository;

    public TenantHealthIndicator(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Health health() {
        try {
            long count = tenantRepository.countByDeletedAtIsNull();
            return Health.up()
                    .withDetail("activeTenants", count)
                    .withDetail("status", "Tenant table accessible")
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }

}
