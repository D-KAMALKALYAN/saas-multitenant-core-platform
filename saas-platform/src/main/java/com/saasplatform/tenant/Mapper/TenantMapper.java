package com.saasplatform.tenant.Mapper;

import com.saasplatform.tenant.dto.TenantResponse;
import com.saasplatform.tenant.entity.Tenant;

public class TenantMapper {

    public static TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .email(tenant.getEmail())
                .plan(tenant.getPlan().name())
                .status(tenant.getStatus().name())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
