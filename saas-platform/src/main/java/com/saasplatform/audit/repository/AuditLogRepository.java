package com.saasplatform.audit.repository;

import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByTenantId(
            UUID tenantId,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndAction(
            UUID tenantId,
            AuditAction action,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndCreatedAtBetween(
            UUID tenantId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<AuditLog> findByTenantIdAndActionAndCreatedAtBetween(
            UUID tenantId,
            AuditAction action,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );
}