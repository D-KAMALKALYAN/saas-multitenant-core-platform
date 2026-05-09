package com.saasplatform.apikey.service;

import com.saasplatform.apikey.dto.ApiKeyCreatedResponse;
import com.saasplatform.apikey.dto.ApiKeyRequest;
import com.saasplatform.apikey.dto.ApiKeyResponse;
import com.saasplatform.apikey.entity.ApiKey;
import com.saasplatform.apikey.entity.ApiKeyStatus;
import com.saasplatform.apikey.repository.ApiKeyRepository;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.exception.ApiKeyNotFoundException;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.service.TenantValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyCacheService cacheService;
    private final TenantValidationService tenantValidationService;

    public StandardApiResponse<ApiKeyCreatedResponse> generateKey(
            ApiKeyRequest request, UUID createdBy) {

        UUID tenantId = TenantContext.getTenantId();

        String rawKey   = generateRawKey();
        String keyHash  = hashKey(rawKey);
        String prefix   = rawKey.substring(0, 16);

        LocalDateTime expiresAt = request.getExpiresInDays() != null
                ? LocalDateTime.now().plusDays(request.getExpiresInDays())
                : null;

        ApiKey apiKey = new ApiKey();
        apiKey.setTenantId(tenantId);
        apiKey.setCreatedBy(createdBy);
        apiKey.setName(request.getName());
        apiKey.setKeyPrefix(prefix);
        apiKey.setKeyHash(keyHash);
        apiKey.setScopes(Collections.singletonList(request.getScopes()));
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setExpiresAt(expiresAt);
        apiKey.setCreatedAt(LocalDateTime.now());

        apiKeyRepository.save(apiKey);

        ApiKeyCreatedResponse response = new ApiKeyCreatedResponse(
                apiKey.getId(), apiKey.getName(), rawKey,
                prefix, request.getScopes(), expiresAt, apiKey.getCreatedAt()
        );

        return StandardApiResponse.success(
                "API key created. Store it safely — shown only once.", response);
    }



    @Async
    public void updateLastUsed(UUID keyId) {
        apiKeyRepository.updateLastUsedAt(keyId, LocalDateTime.now());
    }

    public StandardApiResponse<Void> revokeKey(UUID keyId) {
        UUID tenantId = TenantContext.getTenantId();

        ApiKey key = apiKeyRepository
                .findByIdAndTenantId(keyId, tenantId)
                .orElseThrow(() -> new ApiKeyNotFoundException("API key not found"));

        key.setStatus(ApiKeyStatus.REVOKED);
        key.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(key);

        // Immediate cache invalidation
        cacheService.invalidate(key.getKeyHash());

        return StandardApiResponse.success("API key revoked successfully");
    }

    public StandardApiResponse<List<ApiKeyResponse>> listKeys() {
        UUID tenantId = TenantContext.getTenantId();

        List<ApiKeyResponse> keys = apiKeyRepository
                .findAllByTenantId(tenantId)
                .stream()
                .map(k -> new ApiKeyResponse(
                        k.getId(), k.getName(), k.getKeyPrefix(),
                        k.getStatus().name(), k.getCreatedAt()
                ))
                .toList();

        return StandardApiResponse.success("Success", keys);
    }

    private String generateRawKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "sk_live_" + HexFormat.of().formatHex(bytes);
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}