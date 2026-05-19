package com.saasplatform.tenantconfig.dto;



import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantConfigRequest {

    @NotBlank
    private String value;
}
