package com.saasplatform.common.context;


import java.util.UUID;

public record TenantInfo(
        UUID id,
        String slug,
        String name,
        String plan
) {}