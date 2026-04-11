package com.saasplatform.user.repository;

import com.saasplatform.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    boolean existsByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Page<User> findAllByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<User> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);
}