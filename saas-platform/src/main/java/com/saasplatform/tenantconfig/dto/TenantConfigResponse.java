package com.saasplatform.tenantconfig.dto;



import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TenantConfigResponse {

    private UUID id;
    private String key;
    private String value;
    private LocalDateTime updatedAt;
}