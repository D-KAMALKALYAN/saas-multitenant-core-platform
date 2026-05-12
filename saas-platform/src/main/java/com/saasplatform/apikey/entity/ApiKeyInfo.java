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
// Clean record — no JPA annotations
public record ApiKeyInfo(
        UUID keyId,
        UUID tenantId,
        String tenantSlug,
        String tenantName,
        String plan,
        String scopes,
        String status,
        LocalDateTime expiresAt
) {}