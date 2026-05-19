package com.saasplatform.tenantconfig.service;





import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenantconfig.dto.TenantConfigRequest;
import com.saasplatform.tenantconfig.dto.TenantConfigResponse;
import com.saasplatform.tenantconfig.entity.TenantConfig;
import com.saasplatform.tenantconfig.repository.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantConfigService {

    private final TenantConfigRepository tenantConfigRepository;

    public StandardApiResponse<List<TenantConfigResponse>> getAll() {

        UUID tenantId = TenantContext.getTenantId();

        List<TenantConfigResponse> configs = tenantConfigRepository
                .findAllByTenantId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();

        return StandardApiResponse.success("Configs fetched successfully", configs);
    }

    public StandardApiResponse<TenantConfigResponse> upsert(
            String key,
            TenantConfigRequest request
    ) {

        UUID tenantId = TenantContext.getTenantId();

        TenantConfig config = tenantConfigRepository
                .findByTenantIdAndKey(tenantId, key)
                .map(existing -> {
                    existing.setValue(request.getValue());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return tenantConfigRepository.save(existing);
                })
                .orElseGet(() -> tenantConfigRepository.save(
                        TenantConfig.builder()
                                .tenantId(tenantId)
                                .key(key)
                                .value(request.getValue())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        return StandardApiResponse.success(
                "Config upserted successfully",
                toResponse(config)
        );
    }

    @Transactional
    public StandardApiResponse<Void> delete(String key) {

        UUID tenantId = TenantContext.getTenantId();

        tenantConfigRepository.deleteByTenantIdAndKey(tenantId, key);

        return StandardApiResponse.success(
                "Config deleted successfully",
                null
        );
    }

    private TenantConfigResponse toResponse(TenantConfig config) {

        return TenantConfigResponse.builder()
                .id(config.getId())
                .key(config.getKey())
                .value(config.getValue())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
