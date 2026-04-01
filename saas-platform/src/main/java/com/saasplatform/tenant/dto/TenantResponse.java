package com.saasplatform.tenant.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TenantResponse {

    private UUID id;
    private String name;
    private String slug;
    private String email;

    private String plan;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
