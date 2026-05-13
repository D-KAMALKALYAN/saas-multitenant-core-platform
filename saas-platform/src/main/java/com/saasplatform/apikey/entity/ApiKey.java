package com.saasplatform.apikey.entity;

import com.saasplatform.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "api_keys" , indexes = {
        @Index(name = "idx_api_keys_key_hash" , columnList = "key_hash"),
        @Index(name = "idx_api_keys_tenant_id" , columnList = "tenant_id")
})
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false)
    private Tenant tenant;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    private String name;

    @Column(name = "key_prefix")
    private String keyPrefix;

    @Column(name = "key_hash", unique = true, nullable = false)
    private String keyHash;

    @Column(name = "scopes")
    private String scopes;

    @Enumerated(EnumType.STRING)
    private ApiKeyStatus status;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public List<String> getScopeList() {
        if (scopes == null || scopes.isBlank()) return List.of();
        return Arrays.asList(scopes.split(","));
    }

}



