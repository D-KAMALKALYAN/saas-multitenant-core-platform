package com.saasplatform.audit.aspect;

import com.saasplatform.audit.annotation.Auditable;
import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.audit.entity.AuditStatus;
import com.saasplatform.audit.service.AuditService;
import com.saasplatform.common.context.AuditContext;
import com.saasplatform.common.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object logAudit(
            ProceedingJoinPoint joinPoint,
            Auditable auditable) throws Throwable {

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuditAction action = auditable.action();
        String entityType = auditable.entityType();
        String entityId = extractEntityId(joinPoint);

        try {
            Object result = joinPoint.proceed();

            UUID tenantId = extractTenantId();   // AFTER method
            UUID userId = extractUserId();
            String userEmail = extractUserEmail();

            auditService.log(
                    tenantId,
                    userId,
                    userEmail,
                    action,
                    entityType,
                    entityId,
                    AuditStatus.SUCCESS,
                    ip,
                    userAgent
            );

            return result;

        } catch (Exception ex) {

            UUID tenantId = extractTenantId();
            UUID userId = extractUserId();
            String userEmail = extractUserEmail();

            auditService.log(
                    tenantId,
                    userId,
                    userEmail,
                    action,
                    entityType,
                    entityId,
                    AuditStatus.FAILURE,
                    ip,
                    userAgent
            );

            throw ex;
        }
    }

    private UUID extractTenantId() {
        // Try TenantContext first (authenticated requests)
        try {
            UUID id = TenantContext.getTenantId();
            if (id != null) return id;
        } catch (Exception ignored) {}

        // Fallback to AuditContext (public endpoints like login)
        UUID auditTenantId = AuditContext.getTenantId();
        if (auditTenantId != null) return auditTenantId;

        // Truly no tenant context (system events)
        return null;
    }

    private String extractUserEmail() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;  // return null not "anonymousUser"
        }

        return auth.getName();
    }

    private UUID extractUserId() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }

        Object principal = auth.getPrincipal();

        try {
            return UUID.fromString(principal.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractEntityId(ProceedingJoinPoint joinPoint) {

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UUID uuid) {
                return uuid.toString();
            }
        }

        return null;
    }
}