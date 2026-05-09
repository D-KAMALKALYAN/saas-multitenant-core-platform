package com.saasplatform.apikey.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ApiKeyRequest {

    private String name;

    private String scopes;

    private Integer expiresInDays;

}
