package com.saasplatform.common.filter;

import com.saasplatform.common.context.TenantContext;
import com.saasplatform.usage.service.UsageService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsageTrackingFilter extends OncePerRequestFilter {

    private final UsageService usageService;

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/api/v1/auth"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            if (TenantContext.hasTenant() && shouldTrack(request)) {

                UUID tenantId = TenantContext.getTenantId();

                usageService.track(
                        tenantId,
                        request.getRequestURI(),
                        request.getMethod()
                );
            }

        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldTrack(HttpServletRequest request) {

        String path = request.getRequestURI();

        return EXCLUDED_PREFIXES
                .stream()
                .noneMatch(path::startsWith);
    }
}