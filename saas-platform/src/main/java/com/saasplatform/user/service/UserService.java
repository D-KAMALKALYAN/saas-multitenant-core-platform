package com.saasplatform.user.service;

import com.saasplatform.audit.annotation.Auditable;
import com.saasplatform.audit.entity.AuditAction;
import com.saasplatform.common.context.TenantContext;
import com.saasplatform.common.exception.UserAlreadyExistsException;
import com.saasplatform.common.exception.UserNotFoundException;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.user.dto.UserRequest;
import com.saasplatform.user.dto.UserResponse;
import com.saasplatform.user.dto.UserUpdateRequest;
import com.saasplatform.user.entity.StatusType;
import com.saasplatform.user.entity.User;
import com.saasplatform.user.mapper.UserMapper;
import com.saasplatform.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private static final int MAX_PAGE_SIZE = 100;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Auditable(
            action = AuditAction.USER_CREATED,
            entityType = "USER"
    )
    public StandardApiResponse<UserResponse> createUser(UserRequest request){

        log.info("========== CREATE USER START ==========");
        log.info("Incoming request email: {}", request.getEmail());

        UUID tenantId = TenantContext.getTenantId();
        log.info("TenantContext tenantId: {}", tenantId);

        if (tenantId == null) {
            log.error("TenantId is NULL inside createUser()");
            throw new RuntimeException("Tenant not resolved");
        }

        boolean exists = userRepository.existsByEmailAndTenantId(
                request.getEmail().toLowerCase(),
                tenantId
        );

        log.info("User already exists check result: {}", exists);

        if (exists) {
            log.warn("User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User already exists");
        }


        log.info("Mapping request to entity...");

        User user = UserMapper.toEntity(request);
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail().toLowerCase());

        log.info("Mapping request to entity...");
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        log.info("Saving user to database...");
        User savedUser = userRepository.save(user);

        log.info("User saved successfully with ID: {}", savedUser.getId());

        log.info("Returning success response");
        log.info("========== CREATE USER END ==========");

        return StandardApiResponse.success(
                "User created successfully",
                UserMapper.toResponse(savedUser)
        );
    }

    public StandardApiResponse<List<UserResponse>> getAllUsers(int page , int size) {

        UUID tenantId = TenantContext.getTenantId();

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);

        Page<User> users = userRepository.findAllByTenantIdAndDeletedAtIsNull(tenantId, pageable);

        List<UserResponse> response = users.stream()
                .map(UserMapper::toResponse)
                .toList();

        return StandardApiResponse.success("Success" , response);
    }

    public StandardApiResponse<UserResponse> getUserById(UUID id){

        UUID tenantId = TenantContext.getTenantId();

        User user = userRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id , tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserResponse response = UserMapper.toResponse(user);
        return StandardApiResponse.success("Success" , response);
    }

    @Auditable(
            action = AuditAction.USER_DELETED,
            entityType = "USER"
    )
    public StandardApiResponse<Void> deleteUser(UUID id){

        UUID tenantId = TenantContext.getTenantId();

        User user = userRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id , tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(StatusType.INACTIVE);
        userRepository.save(user);
        return StandardApiResponse.success("User Deleted Successfully");
    }

    @Transactional
    @Auditable(
            action = AuditAction.USER_UPDATED,
            entityType = "USER"
    )
    public StandardApiResponse<UserResponse> updateUser(UUID id, UserUpdateRequest request){

        UUID tenantId = TenantContext.getTenantId();

        User user = userRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id , tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Ownership check for MEMBER role
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();



        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN"));


        if (!isAdmin) {
            // MEMBER — verify they own this resource
            String authenticatedEmail = auth.getName();
            if (!user.getEmail().equalsIgnoreCase(authenticatedEmail)){
                throw new AccessDeniedException(
                        "You can only update your own profile");
            }
        }

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            if (!isAdmin) {
                throw new AccessDeniedException("Only ADMIN can update email");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            if (!isAdmin) {
                throw new AccessDeniedException("Only ADMIN can change roles");
            }
            user.setRole(request.getRole());
        }

        userRepository.save(user);

        UserResponse response = UserMapper.toResponse(user);

        return StandardApiResponse.success("Success" , response);

    }

}