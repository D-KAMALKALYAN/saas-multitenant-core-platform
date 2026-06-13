package com.saasplatform.common.filter;

import com.saasplatform.common.config.JwtService;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.context.TenantInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException, java.io.IOException {

        try {
            // Step 0: If already set (e.g., by another filter), skip
            if (TenantContext.hasTenant()) {
                filterChain.doFilter(request, response);
                return;
            }

            // Step 1: Extract Authorization header
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // No JWT → continue (TenantResolutionFilter may handle)
                filterChain.doFilter(request, response);
                return;
            }

            // Step 2: Extract token
            String token = authHeader.substring(7);

            // Step 3: Validate token
            if (!jwtService.isTokenValid(token)) {
                writeErrorResponse(response, 401, "Invalid or expired token");
                return;
            }

            // Step 4: Extract claims
            Claims claims = jwtService.extractClaims(token);

            String userId   = claims.getSubject();
            String role     = claims.get("role", String.class);
            String email    = claims.get("email", String.class);

            String normalizedRole = role.toUpperCase();

            if(!"SUPER_ADMIN".equals(normalizedRole)){
                String tenantId = claims.get("tenantId", String.class);
                String slug     = claims.get("slug", String.class);
                String plan     = claims.get("plan", String.class);

                if(tenantId == null || tenantId.isBlank()){
                    throw new IllegalStateException("Tenant ID is missing for tenant-scoped user");
                }

                // Step 5: Set TenantContext
                TenantInfo tenantInfo = new TenantInfo(
                        UUID.fromString(tenantId),
                        slug,
                        null,
                        plan
                );

                TenantContext.setTenant(tenantInfo);

                MDC.put("tenantSlug", slug);

            }

            // Step 5.1: Add structured logging context (MDC)

            MDC.put("userId",     userId);
            MDC.put("requestId",  UUID.randomUUID().toString());



            // Step 6: Set Spring SecurityContext
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);




            // Step 7: Continue filter chain
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("JWT authentication failed", ex);
            writeErrorResponse(response, 401, "Authentication failed");
        } finally {
            // VERY IMPORTANT → prevent thread leakage
            TenantContext.clear();

            // VERY IMPORTANT → prevent MDC leakage between requests
            MDC.clear();
        }
    }

    // Helper method
    private void writeErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException, java.io.IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        response.getWriter().write("""
            {
              "status": %d,
              "message": "%s"
            }
            """.formatted(status, message));
    }
}