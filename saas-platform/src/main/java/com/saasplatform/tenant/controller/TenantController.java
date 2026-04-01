package com.saasplatform.tenant.controller;

import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.dto.TenantRequest;
import com.saasplatform.tenant.dto.TenantResponse;
import com.saasplatform.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tenant API", description = "APIs for managing tenants")
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService){
        this.tenantService = tenantService;
    }

    @Operation(
            summary = "Create a new tenant",
            description = "This API creates a tenant with name, email, and slug"
    )
    @PostMapping
    public StandardApiResponse<TenantResponse> createTenant(@Valid @RequestBody TenantRequest request){
        return tenantService.createTenant(request);
    }
}
