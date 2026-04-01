package com.saasplatform.tenant.repository;

import com.saasplatform.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant , Long> {

    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
}
