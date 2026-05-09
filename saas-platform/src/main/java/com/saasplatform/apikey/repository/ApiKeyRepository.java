package com.saasplatform.apikey.repository;

import com.saasplatform.apikey.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    @Query("""
    SELECT ak
    FROM ApiKey ak
    JOIN FETCH ak.tenant
    WHERE ak.keyHash = :keyHash
""")
    Optional<ApiKey> findByKeyHash(@Param("keyHash") String keyHash);

    Optional<ApiKey> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ApiKey> findAllByTenantId(UUID tenantId);

    @Modifying
    @Query("""
        UPDATE ApiKey a
        SET a.lastUsedAt = :time
        WHERE a.id = :keyId
    """)
    void updateLastUsedAt(
            @Param("keyId") UUID keyId,
            @Param("time") LocalDateTime time
    );
}
