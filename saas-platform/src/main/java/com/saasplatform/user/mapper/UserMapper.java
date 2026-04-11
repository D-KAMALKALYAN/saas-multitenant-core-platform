package com.saasplatform.user.mapper;

import com.saasplatform.user.dto.UserRequest;
import com.saasplatform.user.dto.UserResponse;
import com.saasplatform.user.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenant_id(user.getTenantId())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }
}
