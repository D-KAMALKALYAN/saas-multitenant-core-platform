package com.saasplatform.tenant.service;

import com.saasplatform.common.exception.TenantAlreadyExistsException;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.dto.TenantRequest;
import com.saasplatform.tenant.dto.TenantResponse;
import com.saasplatform.tenant.entity.StatusType;
import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;
    }

    public StandardApiResponse<TenantResponse> createTenant(TenantRequest request){


            // Check duplicate slug
            if (tenantRepository.existsBySlug(request.getSlug())) {
                throw new TenantAlreadyExistsException("Tenant with slug '"+ request.getSlug() +  "' already exists");
            }

            // Check duplicate email
            if(tenantRepository.existsByEmail(request.getEmail())){
                throw new TenantAlreadyExistsException("Tenant with email already exists");
            }

            //Convert RequestDTO to Entity
            Tenant savedTenant = new Tenant();

            savedTenant.setName(request.getName());
            savedTenant.setEmail(request.getEmail());
            savedTenant.setSlug(request.getSlug());

            //System Controlled fields
            savedTenant.setPlan(request.getPlan());
            savedTenant.setStatus(StatusType.ACTIVE);

            //Save
            tenantRepository.save(savedTenant);

            //Convert Entity to ResponseDTO;
            TenantResponse response = TenantResponse.builder()
                    .id(savedTenant.getId())
                    .name(savedTenant.getName())
                    .slug(savedTenant.getSlug())
                    .email(savedTenant.getEmail())
                    .plan(savedTenant.getPlan().name())
                    .status(savedTenant.getStatus().name())
                    .createdAt(savedTenant.getCreatedAt())
                    .updatedAt(savedTenant.getUpdatedAt())
                    .build();

            return StandardApiResponse.success("Tenant Created Successfully" , response);
    }
}
