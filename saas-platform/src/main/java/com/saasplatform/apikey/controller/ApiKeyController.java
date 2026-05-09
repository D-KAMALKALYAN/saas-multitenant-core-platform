package com.saasplatform.apikey.controller;

import com.saasplatform.apikey.dto.ApiKeyCreatedResponse;
import com.saasplatform.apikey.dto.ApiKeyRequest;
import com.saasplatform.apikey.dto.ApiKeyResponse;
import com.saasplatform.apikey.service.ApiKeyService;
import com.saasplatform.common.response.StandardApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apikeys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService service;

    @PostMapping
    public StandardApiResponse<ApiKeyCreatedResponse> create(
            @RequestBody ApiKeyRequest req,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return service.generateKey(req, userId);
    }

    @GetMapping
    public StandardApiResponse<List<ApiKeyResponse>> list() {
        return service.listKeys();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @DeleteMapping("/{id}")
    public StandardApiResponse<Void> revoke(@PathVariable UUID id) {
        return service.revokeKey(id);
    }

}