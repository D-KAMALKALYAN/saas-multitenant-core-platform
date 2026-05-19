package com.saasplatform.usage.repository;


import com.saasplatform.usage.entity.UsageRecord;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO usage_records
            (id, tenant_id, record_date, endpoint, method, request_count)
        VALUES
            (gen_random_uuid(), :tenantId, :date, :endpoint, :method, 1)
        ON CONFLICT (tenant_id, record_date, endpoint, method)
        DO UPDATE
        SET request_count = usage_records.request_count + 1
        """, nativeQuery = true)
    void upsertCount(
            @Param("tenantId") UUID tenantId,
            @Param("date") LocalDate date,
            @Param("endpoint") String endpoint,
            @Param("method") String method
    );

    List<UsageRecord> findByTenantIdAndRecordDateBetween(
            UUID tenantId,
            LocalDate from,
            LocalDate to
    );

    List<UsageRecord> findByRecordDateBetween(
            LocalDate from,
            LocalDate to
    );
}
