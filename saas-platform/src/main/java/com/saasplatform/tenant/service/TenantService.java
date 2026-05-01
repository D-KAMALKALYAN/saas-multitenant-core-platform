package com.saasplatform.tenant.service;

import com.saasplatform.audit.annotation.Auditable;
import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.common.exception.TenantAlreadyExistsException;
import com.saasplatform.common.exception.TenantNotFoundException;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.mapper.TenantMapper;
import com.saasplatform.tenant.dto.TenantRequest;
import com.saasplatform.tenant.dto.TenantResponse;
import com.saasplatform.tenant.dto.TenantUpdateRequest;
import com.saasplatform.tenant.entity.StatusType;
import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    private static final int MAX_PAGE_SIZE = 100;

    public TenantService(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;
    }


    @Auditable(
            action = AuditAction.TENANT_CREATED,
            entityType = "TENANT"
    )
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

            TenantResponse response = TenantMapper.toResponse(savedTenant);

            return StandardApiResponse.success("Tenant Created Successfully" , response);
    }

    public StandardApiResponse<TenantResponse> getTenantById(UUID id){

        Tenant tenant = tenantRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        TenantResponse response = TenantMapper.toResponse(tenant);

        return StandardApiResponse.success("Success", response);
    }

    public StandardApiResponse<List<TenantResponse>> getAllTenants(int page, int size) {

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<Tenant> tenants = tenantRepository.findAllByDeletedAtIsNull(pageable);

        List<TenantResponse> response = tenants.stream()
                .map(TenantMapper::toResponse)
                .collect(Collectors.toList());

        return StandardApiResponse.success("Success", response);
    }

    @Auditable(
            action = AuditAction.TENANT_DELETED,
            entityType = "TENANT"
    )
    public StandardApiResponse<Void> deleteTenant(UUID id){

        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not Found"));

        tenant.setDeletedAt(LocalDateTime.now());
        tenant.setStatus(StatusType.INACTIVE);
        tenantRepository.save(tenant);

        return StandardApiResponse.success("Tenant Deleted Successfully");

    }

    @Transactional
    @Auditable(
            action = AuditAction.TENANT_UPDATED,
            entityType = "TENANT"
    )
    public StandardApiResponse<TenantResponse> updateTenant(UUID id , TenantUpdateRequest request){
        Tenant tenant = tenantRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant Not Found"));

        //Check if Email is unique with other tenants
        if (request.getEmail() != null &&
                tenantRepository.existsByEmailAndIdNot(request.getEmail() , id)){
            throw new TenantAlreadyExistsException("Tenant with email already exists");
        }

        if (request.getName() != null) {
            tenant.setName(request.getName());
        }

        if (request.getEmail() != null) {
            tenant.setEmail(request.getEmail());
        }

        if (request.getPlan() != null) {
            tenant.setPlan(request.getPlan());
        }

        tenantRepository.save(tenant);

        TenantResponse response = TenantMapper.toResponse(tenant);

        return StandardApiResponse.success("Updated Successfully" , response);
    }

}
