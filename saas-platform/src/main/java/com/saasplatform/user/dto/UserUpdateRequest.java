package com.saasplatform.user.dto;


import com.saasplatform.user.entity.RoleType;
import com.saasplatform.user.entity.StatusType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String email;

    @Nullable
    private RoleType role;

    @Nullable
    private StatusType status;
}
