package com.saasplatform.tenant.service;

import com.saasplatform.common.exception.TenantNotActiveException;
import com.saasplatform.common.exception.TenantNotFoundException;
import com.saasplatform.tenant.entity.StatusType;
import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantValidationService {

    private final TenantRepository tenantRepository;

    public TenantValidationService(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;
    }

    public Tenant getActiveTenantBySlug(String slug){

        Tenant tenant = tenantRepository
                .findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new TenantNotFoundException("Tenant Not Found"));

        if(!tenant.getStatus().equals(StatusType.ACTIVE)){
            throw new TenantNotActiveException("Tenant Not Active");
        }

        return tenant;
    }
}
