package com.saasplatform.tenant;

import com.saasplatform.common.exception.TenantAlreadyExistsException;
import com.saasplatform.tenant.dto.TenantRequest;
import com.saasplatform.tenant.entity.PlanType;
import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;
import com.saasplatform.tenant.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TenantServiceTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void cleanup() {
        tenantRepository.deleteAll();
    }

    @Test
    void createTenant_withValidRequest_createsTenant() {

        TenantRequest request = TenantRequest.builder()
                .name("Test Company")
                .slug("test-co")
                .email("admin@testco.com")
                .plan(PlanType.BASIC)
                .build();

        tenantService.createTenant(request);

        Tenant tenant = tenantRepository
                .findBySlugAndDeletedAtIsNull("test-co")
                .orElse(null);

        assertNotNull(tenant);
        assertEquals("Test Company", tenant.getName());
        assertEquals("test-co", tenant.getSlug());
        assertEquals("admin@testco.com", tenant.getEmail());
        assertEquals(PlanType.BASIC, tenant.getPlan());
    }

    @Test
    void createTenant_withDuplicateSlug_throwsConflict() {

        tenantService.createTenant(
                TenantRequest.builder()
                        .name("Company One")
                        .slug("test-co")
                        .email("one@test.com")
                        .plan(PlanType.BASIC)
                        .build()
        );

        TenantRequest duplicateRequest = TenantRequest.builder()
                .name("Company Two")
                .slug("test-co")
                .email("two@test.com")
                .plan(PlanType.PREMIUM)
                .build();

        assertThrows(
                TenantAlreadyExistsException.class,
                () -> tenantService.createTenant(duplicateRequest)
        );
    }

    @Test
    void createTenant_withDuplicateEmail_throwsConflict() {

        tenantService.createTenant(
                TenantRequest.builder()
                        .name("Company One")
                        .slug("company-one")
                        .email("admin@test.com")
                        .plan(PlanType.BASIC)
                        .build()
        );

        TenantRequest duplicateEmail = TenantRequest.builder()
                .name("Company Two")
                .slug("company-two")
                .email("admin@test.com")
                .plan(PlanType.BASIC)
                .build();

        assertThrows(
                TenantAlreadyExistsException.class,
                () -> tenantService.createTenant(duplicateEmail)
        );
    }

    @Test
    void createTenant_persistsTenantInDatabase() {

        TenantRequest request = TenantRequest.builder()
                .name("Acme Corporation")
                .slug("acme")
                .email("admin@acme.com")
                .plan(PlanType.BASIC)
                .build();

        tenantService.createTenant(request);

        assertEquals(1, tenantRepository.count());
    }

    @Test
    void createTenant_multipleUniqueTenants_succeeds() {

        tenantService.createTenant(
                TenantRequest.builder()
                        .name("Company A")
                        .slug("company-a")
                        .email("admin@companya.com")
                        .plan(PlanType.BASIC)
                        .build()
        );

        tenantService.createTenant(
                TenantRequest.builder()
                        .name("Company B")
                        .slug("company-b")
                        .email("admin@companyb.com")
                        .plan(PlanType.PREMIUM)
                        .build()
        );

        assertEquals(2, tenantRepository.count());
    }

    @Test
    void createTenant_storesAllFieldsCorrectly() {

        TenantRequest request = TenantRequest.builder()
                .name("GE Aerospace")
                .slug("ge-aerospace")
                .email("admin@ge.com")
                .plan(PlanType.PREMIUM)
                .build();

        tenantService.createTenant(request);

        Tenant tenant = tenantRepository
                .findBySlugAndDeletedAtIsNull("ge-aerospace")
                .orElseThrow();

        assertEquals("GE Aerospace", tenant.getName());
        assertEquals("ge-aerospace", tenant.getSlug());
        assertEquals("admin@ge.com", tenant.getEmail());
        assertEquals(PlanType.PREMIUM, tenant.getPlan());

        assertNotNull(tenant.getId());
        assertNotNull(tenant.getCreatedAt());
        assertNotNull(tenant.getUpdatedAt());
    }
}