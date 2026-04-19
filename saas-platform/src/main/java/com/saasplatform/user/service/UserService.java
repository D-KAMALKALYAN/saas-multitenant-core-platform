package com.saasplatform.user.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final int MAX_PAGE_SIZE = 100;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public StandardApiResponse<UserResponse> createUser(UserRequest request){

        UUID tenantId = TenantContext.getTenantId();

        if (userRepository.existsByEmailAndTenantId(
                request.getEmail().toLowerCase(), tenantId)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User user = UserMapper.toEntity(request);
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail().toLowerCase());
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        User savedUser = userRepository.save(user);

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
    public StandardApiResponse<UserResponse> updateUser(UUID id, UserUpdateRequest request){

        UUID tenantId = TenantContext.getTenantId();

        User user = userRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id , tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null){
            user.setFirstName(request.getLastName());
        }

        if(request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        if(request.getRole() != null){
            user.setRole(request.getRole());
        }

        userRepository.save(user);

        UserResponse response = UserMapper.toResponse(user);

        return StandardApiResponse.success("Success" , response);

    }

}