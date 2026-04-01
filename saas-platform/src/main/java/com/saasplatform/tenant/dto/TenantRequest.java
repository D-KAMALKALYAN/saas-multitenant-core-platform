package com.saasplatform.tenant.dto;

import com.saasplatform.tenant.entity.PlanType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TenantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 100, message = "Slug must be at most 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase and hyphen-separated")
    private String slug;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private PlanType plan;
}
