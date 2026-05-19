package com.saasplatform.usage.service;


import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.usage.entity.UsageRecord;
import com.saasplatform.usage.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

        usageRecordRepository.upsertCount(
                tenantId,
                LocalDate.now(),
                endpoint,
                method
        );
    }

    public StandardApiResponse<?> getUsage(
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

        if (isSuperAdmin) {

            List<UsageRecord> all = usageRecordRepository
                    .findByRecordDateBetween(from, to);

            return StandardApiResponse.success(
                    "Usage fetched successfully",
                    all
            );
        }

        UUID tenantId = TenantContext.getTenantId();

        List<UsageRecord> tenantUsage = usageRecordRepository
                .findByTenantIdAndRecordDateBetween(
                        tenantId,
                        from,
                        to
                );

        return StandardApiResponse.success(
                "Usage fetched successfully",
                tenantUsage
        );
    }
}
