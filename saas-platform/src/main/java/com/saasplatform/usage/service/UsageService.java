package com.saasplatform.usage.service;


import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.usage.dto.UsageResponse;
import com.saasplatform.usage.entity.UsageRecord;
import com.saasplatform.usage.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;



@Slf4j
@Service
@RequiredArgsConstructor
public class UsageService {

    private final UsageRecordRepository usageRecordRepository;

    @Async
    public void track(
            UUID tenantId,
            String endpoint,
            String method
    ) {

        try {

            usageRecordRepository.upsertCount(
                    tenantId,
                    LocalDate.now(),
                    endpoint,
                    method
            );

            log.debug(
                    "Usage tracked successfully -> tenant={} endpoint={} method={}",
                    tenantId,
                    endpoint,
                    method
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to track usage -> tenant={} endpoint={} method={}",
                    tenantId,
                    endpoint,
                    method,
                    ex
            );
        }
    }

    public StandardApiResponse<List<UsageResponse>> getUsage(
            LocalDate from,
            LocalDate to
    ) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean isSuperAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_SUPER_ADMIN"));

        List<UsageRecord> records;

        if (isSuperAdmin) {

            records = usageRecordRepository
                    .findByRecordDateBetween(from, to);

        } else {

            UUID tenantId = TenantContext.getTenantId();

            records = usageRecordRepository
                    .findByTenantIdAndRecordDateBetween(
                            tenantId,
                            from,
                            to
                    );
        }

        List<UsageResponse> response = records.stream()
                .map(this::mapToResponse)
                .toList();

        return StandardApiResponse.success(
                "Usage fetched successfully",
                response
        );
    }


    private UsageResponse mapToResponse(
            UsageRecord record
    ) {

        return UsageResponse.builder()
                .tenantId(record.getTenantId())
                .recordDate(record.getRecordDate())
                .endpoint(record.getEndpoint())
                .method(record.getMethod())
                .requestCount(record.getRequestCount())
                .build();
    }
}
