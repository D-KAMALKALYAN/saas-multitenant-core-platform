package com.saasplatform.user.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
    private UUID tenant_id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
