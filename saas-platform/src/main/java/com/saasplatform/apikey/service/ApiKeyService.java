package com.saasplatform.apikey.service;

import com.saasplatform.apikey.dto.ApiKeyCreatedResponse;
import com.saasplatform.apikey.dto.ApiKeyRequest;
import com.saasplatform.apikey.dto.ApiKeyResponse;
import com.saasplatform.apikey.entity.ApiKey;
import com.saasplatform.apikey.entity.ApiKeyStatus;
import com.saasplatform.apikey.repository.ApiKeyRepository;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.response.StandardApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
    private final ApiKeyCacheService apiKeyCacheService;

    // Generate raw key
    private String generateRawKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return "sk_live_" + HexFormat.of().formatHex(bytes);
    }

    // Hash key
    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // Generate API Key
    public StandardApiResponse<ApiKeyCreatedResponse> generateKey(ApiKeyRequest request, UUID userId) {

        String rawKey = generateRawKey();
        String hash = hashKey(rawKey);

        String prefix = rawKey.substring(0, 12);

        ApiKey key = new ApiKey();
        key.setTenantId(TenantContext.getTenantId());
        key.setCreatedBy(userId);
        key.setName(request.getName());
        key.setKeyPrefix(prefix);
        key.setKeyHash(hash);
        key.setScopes(Collections.singletonList(request.getScopes()));
        key.setStatus(ApiKeyStatus.ACTIVE);
        key.setCreatedAt(LocalDateTime.now());

        if (request.getExpiresInDays() > 0) {
            key.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
        }

        apiKeyRepository.save(key);

        return StandardApiResponse.success(
                "API key created successfully",
                new ApiKeyCreatedResponse(rawKey)
        );
    }

    // List keys
    public StandardApiResponse<List<ApiKeyResponse>> listKeys() {

        UUID tenantId = TenantContext.getTenantId();

        List<ApiKeyResponse> keys = apiKeyRepository.findAllByTenantId(tenantId)
                .stream()
                .map(k -> new ApiKeyResponse(
                        k.getId(),
                        k.getName(),
                        k.getKeyPrefix(),
                        k.getStatus().name(),
                        k.getCreatedAt()
                ))
                .toList();

        return StandardApiResponse.success(
                "API keys fetched successfully",
                keys
        );
    }

    // Revoke key
    public StandardApiResponse<Void> revokeKey(UUID keyId) {

        UUID tenantId = TenantContext.getTenantId();

        ApiKey key = apiKeyRepository
                .findByIdAndTenantId(keyId, tenantId)
                .orElseThrow(() -> new RuntimeException("API key not found"));

        key.setStatus(ApiKeyStatus.REVOKED);
        key.setRevokedAt(LocalDateTime.now());

        apiKeyRepository.save(key);

        apiKeyCacheService.invalidate(key.getKeyHash());

        return StandardApiResponse.success("API key revoked successfully");
    }


    @Async
    @Transactional
    public void updateLastUsed(UUID keyId) {

        apiKeyRepository.updateLastUsedAt(
                keyId,
                LocalDateTime.now()
        );
    }
}