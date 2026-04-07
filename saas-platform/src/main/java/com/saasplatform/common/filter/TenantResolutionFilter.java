package com.saasplatform.common.filter;

import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.exception.TenantNotActiveException;
import com.saasplatform.common.exception.TenantNotFoundException;
import com.saasplatform.tenant.service.TenantValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    private final TenantValidationService tenantValidationService;
    private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);

    public TenantResolutionFilter(TenantValidationService tenantValidationService){
        this.tenantValidationService = tenantValidationService;
    }

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/tenants",        // tenant creation
            "/swagger-ui",            // swagger
            "/v3/api-docs",           // openapi docs
            "/actuator/health"        // health check
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("🔍 TenantFilter triggered for path: {}", path);

        try {
            // 1. Check excluded paths
            if (isExcludedPath(path)) {
                log.warn("⛔ Skipping filter (excluded path): {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Get header
            String tenantSlug = request.getHeader("X-Tenant-ID");
            log.info("📌 Header X-Tenant-ID: {}", tenantSlug);

            if (tenantSlug == null || tenantSlug.isBlank()) {
                log.error("❌ Missing tenant header");
                writeErrorResponse(response, 400, "Missing X-Tenant-ID header");
                return;
            }

            // 3. Validate tenant
            try {
                log.info("🔎 Validating tenant: {}", tenantSlug);
                tenantValidationService.getActiveTenantBySlug(tenantSlug);
                log.info("✅ Tenant validation passed");
            } catch (TenantNotFoundException ex) {
                log.error("❌ Tenant not found: {}", tenantSlug);
                writeErrorResponse(response, 404, ex.getMessage());
                return;
            } catch (TenantNotActiveException ex) {
                log.error("❌ Tenant inactive: {}", tenantSlug);
                writeErrorResponse(response, 403, ex.getMessage());
                return;
            }

            // 4. Set context
            TenantContext.setTenantId(tenantSlug);
            log.info("✅ TenantContext set: {}", tenantSlug);

            // 5. Continue
            filterChain.doFilter(request, response);

        } finally {
            log.info("🧹 Clearing TenantContext");
            TenantContext.clear();
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::equals);
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + message + "\"}"
        );
    }
}
