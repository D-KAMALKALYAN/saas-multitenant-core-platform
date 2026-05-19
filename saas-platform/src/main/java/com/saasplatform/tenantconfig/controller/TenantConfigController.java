package com.saasplatform.tenantconfig.controller;




import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenantconfig.dto.TenantConfigRequest;
import com.saasplatform.tenantconfig.dto.TenantConfigResponse;
import com.saasplatform.tenantconfig.service.TenantConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant-config")
@RequiredArgsConstructor
public class TenantConfigController {

    private final TenantConfigService tenantConfigService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public StandardApiResponse<List<TenantConfigResponse>> getAll() {

        return tenantConfigService.getAll();
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public StandardApiResponse<TenantConfigResponse> upsert(
            @PathVariable String key,
            @Valid @RequestBody TenantConfigRequest request
    ) {

        return tenantConfigService.upsert(key, request);
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public StandardApiResponse<Void> delete(
            @PathVariable String key
    ) {

        return tenantConfigService.delete(key);
    }
}
