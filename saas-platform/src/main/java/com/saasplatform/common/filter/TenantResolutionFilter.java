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

    private static final List<String[]> EXCLUDED_PATHS = List.of(
            new String[]{"POST",  "/api/v1/tenants"},   // only POST is public
            new String[]{"GET",   "/swagger-ui"},
            new String[]{"GET",   "/v3/api-docs"},
            new String[]{"GET",   "/actuator/health"}
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("Tenant filter triggered -> {} {}", method, path);

        try {
            // 1. Check excluded paths
            if (isExcludedPath(request)) {
                log.warn("Request excluded from tenant validation -> {} {}", method, path);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Read header
            String tenantSlug = request.getHeader("X-Tenant-ID");

            if (tenantSlug == null || tenantSlug.isBlank()) {
                log.error("Missing X-Tenant-ID header");
                writeErrorResponse(response, 400, "Missing X-Tenant-ID header");
                return;
            }

            // 3. Validate tenant
            try {
                tenantValidationService.getActiveTenantBySlug(tenantSlug);

            } catch (TenantNotFoundException ex) {
                log.error("Tenant not found -> {}", tenantSlug);
                writeErrorResponse(response, 404, ex.getMessage());
                return;

            } catch (TenantNotActiveException ex) {
                log.error("Tenant not active -> {}", tenantSlug);
                writeErrorResponse(response, 403, ex.getMessage());
                return;
            }

            // 4. Set context
            TenantContext.setTenantId(tenantSlug);

            // 5. Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        String path   = request.getRequestURI();
        String method = request.getMethod();

        return EXCLUDED_PATHS.stream().anyMatch(entry ->
                method.equalsIgnoreCase(entry[0]) &&
                        path.startsWith(entry[1])
        );
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