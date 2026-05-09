package com.saasplatform.apikey.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

   private UUID id;

   private String name;

   private String keyPrefix;

   private String status;

   private LocalDateTime createdAt;


}
