package com.saasplatform.common.bootstrap;

import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;
import com.saasplatform.user.entity.RoleType;
import com.saasplatform.user.entity.StatusType;
import com.saasplatform.user.entity.User;
import com.saasplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootstrapService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-email:admin@platform.internal}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Value("${app.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrap() {
        if (!bootstrapEnabled) {
            log.info("Bootstrap disabled via configuration");
            return;
        }

        boolean superAdminExists = userRepository
                .existsByRole(RoleType.SUPER_ADMIN);

        if (superAdminExists) {
            log.info("SUPER_ADMIN already exists — skipping bootstrap");
            return;
        }

        log.warn("No SUPER_ADMIN found — running bootstrap");

        Tenant platformTenant = tenantRepository
                .findBySlugAndDeletedAtIsNull("platform")
                .orElseThrow(() -> new IllegalStateException(
                        "Platform tenant missing — V7 migration may not have run"));

        User superAdmin = User.builder()
                .tenantId(platformTenant.getId())
                .firstName("Platform")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(RoleType.SUPER_ADMIN)
                .status(StatusType.ACTIVE)
                .build();

        userRepository.save(superAdmin);

        log.warn("Bootstrap complete — SUPER_ADMIN created: {}", adminEmail);
        log.warn("Change the admin password immediately after first login");
    }

}
