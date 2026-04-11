package com.saasplatform.user.dto;

import com.saasplatform.user.entity.RoleType;
import com.saasplatform.user.entity.StatusType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "first name is required")
    private String firstName;

    @NotBlank(message = "first name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    @NotNull(message = "role is required")
    private RoleType role;
}
