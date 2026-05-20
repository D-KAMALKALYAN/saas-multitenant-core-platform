package com.saasplatform.usage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class UsageResponse {
    private UUID tenantId;
    private LocalDate recordDate;
    private String endpoint;
    private String method;
    private Long requestCount;
}