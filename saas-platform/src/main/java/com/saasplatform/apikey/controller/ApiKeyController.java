package com.saasplatform.apikey.controller;

import com.saasplatform.apikey.dto.ApiKeyCreatedResponse;
import com.saasplatform.apikey.dto.ApiKeyRequest;
import com.saasplatform.apikey.dto.ApiKeyResponse;
import com.saasplatform.apikey.service.ApiKeyService;
import com.saasplatform.common.response.StandardApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apikeys")
@RequiredArgsConstructor
@Tag(
        name = "API Key API",
        description = "APIs for generating, listing, and revoking API keys"
)
public class ApiKeyController {

    private final ApiKeyService service;

    @Operation(
            summary = "Generate a new API key",
            description = "Creates a new API key for the authenticated user. " +
                    "Only SUPER_ADMIN and ADMIN users are allowed to generate API keys."
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping
    public StandardApiResponse<ApiKeyCreatedResponse> create(
            @RequestBody ApiKeyRequest req,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());
        return service.generateKey(req, userId);
    }

    @Operation(
            summary = "Get all API keys",
            description = "Retrieves all active API keys for the current tenant. " +
                    "Only SUPER_ADMIN and ADMIN users can access this endpoint."
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping
    public StandardApiResponse<List<ApiKeyResponse>> list() {
        return service.listKeys();
    }

    @Operation(
            summary = "Revoke API key",
            description = "Revokes an API key using its UUID. " +
                    "The revoked key can no longer be used for authentication."
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public StandardApiResponse<Void> revoke(@PathVariable UUID id) {
        return service.revokeKey(id);
    }

}