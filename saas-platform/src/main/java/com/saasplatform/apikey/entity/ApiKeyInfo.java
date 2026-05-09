package com.saasplatform.apikey.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight immutable object stored in cache.
 *
 * Why this exists:
 * - Avoid hitting DB on every API request
 * - Store only authentication-related metadata
 * - Never cache sensitive raw API keys
 */
public record ApiKeyInfo(

        // API key ID
        UUID keyId,

        // Tenant owning this key
        UUID tenantId,

        // Used for TenantContext + routing
        String tenantSlug,

        // Display / logging purposes
        String tenantName,

        // Tenant subscription plan
        String plan,

        @ElementCollection(fetch = FetchType.EAGER)
        List<String> scopes,

        // ACTIVE / REVOKED
        String status,

        // Expiration validation
        LocalDateTime expiresAt

) {
}