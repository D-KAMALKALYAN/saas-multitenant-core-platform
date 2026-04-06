package com.saasplatform.tenant.repository;

import com.saasplatform.tenant.entity.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant , UUID> {

    boolean existsByEmail(String email);

    boolean existsBySlug(String slug);

    Optional<Tenant> findByIdAndDeletedAtIsNull(UUID id);

    Page<Tenant> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
