package com.saasplatform.audit.service;

import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.audit.entity.AuditLog;
import com.saasplatform.audit.entity.AuditStatus;
import com.saasplatform.audit.repository.AuditLogRepository;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.response.StandardApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            UUID tenantId,
            UUID userId,
            String userEmail,
            AuditAction action,
            String entityType,
            String entityId,
            AuditStatus status,
            String ipAddress,
            String userAgent
    ) {

        try {
            AuditLog auditLog = new AuditLog();

            auditLog.setTenantId(tenantId);
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setStatus(status.name());
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);

            auditLogRepository.save(auditLog);

        } catch (Exception ex) {
            log.error("Failed to save audit log", ex);
        }
    }

    public StandardApiResponse<?> getAuditLogs(
            int page,
            int size,
            AuditAction action,
            LocalDateTime from,
            LocalDateTime to
    ) {

        UUID tenantId = extractTenantId();

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable =
                PageRequest.of(safePage, safeSize);

        Page<AuditLog> logs;

        if (action != null && from != null && to != null) {

            logs = auditLogRepository
                    .findByTenantIdAndActionAndCreatedAtBetween(
                            tenantId,
                            action,
                            from,
                            to,
                            pageable
                    );

        } else if (action != null) {

            logs = auditLogRepository
                    .findByTenantIdAndAction(
                            tenantId,
                            action,
                            pageable
                    );

        } else if (from != null && to != null) {

            logs = auditLogRepository
                    .findByTenantIdAndCreatedAtBetween(
                            tenantId,
                            from,
                            to,
                            pageable
                    );

        } else {

            logs = auditLogRepository
                    .findByTenantId(
                            tenantId,
                            pageable
                    );
        }

        return StandardApiResponse.success(
                "Audit logs fetched successfully",
                logs
        );
    }

    private UUID extractTenantId() {

        try {
            return TenantContext.getTenantId();
        } catch (Exception ex) {
            return null;
        }
    }

    private UUID extractUserId() {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (auth == null) {
            return null;
        }

        try {
            return UUID.fromString(auth.getName());
        } catch (Exception ex) {
            return null;
        }
    }
}