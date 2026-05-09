package com.saasplatform.apikey.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyCreatedResponse {

    private UUID id;
    private String name;
    private String apiKey;      // full key — shown once only
    private String keyPrefix;   // for future identification
    private String scopes;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

}
