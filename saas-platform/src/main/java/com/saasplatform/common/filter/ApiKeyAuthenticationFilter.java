package com.saasplatform.common.filter;

import com.saasplatform.apikey.entity.ApiKey;
import com.saasplatform.apikey.entity.ApiKeyInfo;
import com.saasplatform.apikey.entity.ApiKeyStatus;
import com.saasplatform.apikey.repository.ApiKeyRepository;
import com.saasplatform.apikey.service.ApiKeyCacheService;
import com.saasplatform.apikey.service.ApiKeyService;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.context.TenantInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyCacheService apiKeyCacheService;
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String rawKey = request.getHeader("X-API-Key");

            // No API key → skip filter
            if (rawKey == null || rawKey.isBlank()) {

                log.debug("No API key present in request");

                filterChain.doFilter(request, response);
                return;
            }

            log.info("API key authentication started");

            String keyHash = sha256(rawKey);

            // CACHE FIRST
            ApiKeyInfo keyInfo = apiKeyCacheService.get(keyHash);

            if (keyInfo != null) {

                log.info(
                        "CACHE HIT :: tenant={} :: keyId={}",
                        keyInfo.tenantSlug(),
                        keyInfo.keyId()
                );

            } else {

                log.warn("CACHE MISS :: querying database");

                keyInfo = loadFromDb(keyHash);
            }

            if (keyInfo == null) {

                log.error("Invalid API key");

                unauthorized(response, "Invalid API key");
                return;
            }

            // STATUS CHECK
            if (!ApiKeyStatus.ACTIVE.name()
                    .equals(keyInfo.status())) {

                log.warn(
                        "Revoked API key used :: keyId={}",
                        keyInfo.keyId()
                );

                unauthorized(response, "API key revoked");
                return;
            }

            // EXPIRY CHECK
            if (keyInfo.expiresAt() != null &&
                    keyInfo.expiresAt().isBefore(LocalDateTime.now())) {

                log.warn(
                        "Expired API key used :: keyId={}",
                        keyInfo.keyId()
                );

                unauthorized(response, "API key expired");
                return;
            }

            // TENANT CONTEXT
            TenantContext.setTenant(
                    new TenantInfo(
                            keyInfo.tenantId(),
                            keyInfo.tenantSlug(),
                            keyInfo.tenantName(),
                            keyInfo.plan()
                    )
            );

            log.info(
                    "Tenant context set :: tenant={}",
                    keyInfo.tenantSlug()
            );

            // SECURITY CONTEXT
            List<SimpleGrantedAuthority> authorities =
                    new ArrayList<>();

            // Parse CSV scopes
            if (keyInfo.scopes() != null &&
                    !keyInfo.scopes().isBlank()) {

                List<SimpleGrantedAuthority> scopeAuthorities =
                        List.of(keyInfo.scopes().split(","))
                                .stream()
                                .map(String::trim)
                                .filter(scope -> !scope.isBlank())
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                authorities.addAll(scopeAuthorities);
            }

            // System role for API clients
            authorities.add(
                    new SimpleGrantedAuthority("ROLE_API")
            );

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            keyInfo.tenantSlug(),
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);

            log.info(
                    "Authentication success :: tenant={} :: scopes={}",
                    keyInfo.tenantSlug(),
                    keyInfo.scopes()
            );

            // ASYNC LAST USED UPDATE
            apiKeyService.updateLastUsed(keyInfo.keyId());

            filterChain.doFilter(request, response);

        } finally {

            TenantContext.clear();

            SecurityContextHolder.clearContext();

            log.debug("Security context cleared");
        }
    }

    private ApiKeyInfo loadFromDb(String keyHash) {

        log.info("DB HIT :: fetching API key from database");

        Optional<ApiKey> optionalKey =
                apiKeyRepository.findByKeyHash(keyHash);

        if (optionalKey.isEmpty()) {

            log.warn("API key not found in database");

            return null;
        }

        ApiKey key = optionalKey.get();

        ApiKeyInfo info = new ApiKeyInfo(
                key.getId(),
                key.getTenant().getId(),
                key.getTenant().getSlug(),
                key.getTenant().getName(),
                key.getTenant().getPlan().name(),
                key.getScopes(),
                key.getStatus().name(),
                key.getExpiresAt()
        );

        // Store in cache
        apiKeyCacheService.put(keyHash, info);

        log.info(
                "API key cached :: tenant={} :: keyId={}",
                info.tenantSlug(),
                info.keyId()
        );

        return info;
    }

    private String sha256(String input) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            input.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {

            throw new RuntimeException("Failed to hash API key", e);
        }
    }

    private void unauthorized(HttpServletResponse response,
                              String message)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        response.getWriter().write(
                """
                {
                    "success": false,
                    "message": "%s"
                }
                """.formatted(message)
        );
    }
}

