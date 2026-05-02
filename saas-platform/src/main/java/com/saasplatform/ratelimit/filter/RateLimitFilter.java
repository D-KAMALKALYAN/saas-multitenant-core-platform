package com.saasplatform.ratelimit.filter;

import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.context.TenantInfo;
import com.saasplatform.ratelimit.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip public endpoints — no TenantContext set
        if (!TenantContext.hasTenant()) {
            filterChain.doFilter(request, response);
            return;
        }

        TenantInfo tenant = TenantContext.getTenant();
        Bucket bucket = rateLimitService
                .resolveBucket(tenant.slug(), tenant.plan());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for tenant: {}", tenant.slug());
            writeRateLimitResponse(response, retryAfter);
        }
    }

    private void writeRateLimitResponse(
            HttpServletResponse response,
            long retryAfterSeconds) throws IOException {

        response.setStatus(429);
        response.setContentType("application/json");
        response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Rate limit exceeded. " +
                        "Retry after " + retryAfterSeconds + " seconds.\"}");
    }
}
