package com.saasplatform.tenant.controller;

import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.dto.TenantRequest;
import com.saasplatform.tenant.dto.TenantResponse;
import com.saasplatform.tenant.dto.TenantUpdateRequest;
import com.saasplatform.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<StandardApiResponse<TenantResponse>> createTenant(@Valid @RequestBody TenantRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request));
    }

    @Operation(
            summary = "Get tenant by ID",
            description = "Retrieves tenant details for the given tenant ID. Returns 404 if the tenant does not exist or is soft-deleted."
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<StandardApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tenantService.getTenantById(id));
    }


    @Operation(
            summary = "Get all tenants with pagination",
            description = "Retrieves a paginated list of tenants where deleted_at is null. " +
                    "Accepts page number and size as query parameters. " +
                    "Throws 404 if no tenants are found."
    )

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN' , 'MEMBER' , 'VIEWER')")
    public ResponseEntity<StandardApiResponse<List<TenantResponse>>> getAllTenants(@RequestParam(defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "10") int size){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tenantService.getAllTenants(page, size));
    }


    @Operation(
            summary = "Soft-delete a tenant by ID",
            description = "Marks the tenant with the given UUID as deleted (soft-delete). " +
                    "Returns HTTP 200 on successful deletion. " +
                    "Throws 404 if the tenant does not exist or is already deleted."
    )
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardApiResponse<Void>> deleteTenant(@PathVariable UUID id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tenantService.deleteTenant(id));
    }

    @Operation(
            summary = "Update Tenant",
            description = "Partially updates tenant details like name, email, or plan"
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<StandardApiResponse<TenantResponse>> updateTenant(@PathVariable UUID id , @Valid @RequestBody TenantUpdateRequest request){
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(tenantService.updateTenant(id , request));
    }

}
