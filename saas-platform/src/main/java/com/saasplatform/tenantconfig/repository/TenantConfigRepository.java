package com.saasplatform.tenantconfig.repository;

import com.saasplatform.tenantconfig.entity.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface TenantConfigRepository extends JpaRepository<TenantConfig, UUID> {

    Optional<TenantConfig> findByTenantIdAndKey(UUID tenantId, String key);

    List<TenantConfig> findAllByTenantId(UUID tenantId);

    void deleteByTenantIdAndKey(UUID tenantId, String key);
}
