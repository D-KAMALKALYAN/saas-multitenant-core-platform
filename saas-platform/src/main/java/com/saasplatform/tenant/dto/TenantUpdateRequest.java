package com.saasplatform.tenant.dto;

import com.saasplatform.tenant.entity.PlanType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TenantUpdateRequest {

    @Nullable
    private String name;

    @Nullable
    private String email;

    @Nullable
    private PlanType plan;
}
