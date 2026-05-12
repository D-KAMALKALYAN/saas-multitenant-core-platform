package com.saasplatform.apikey.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Data
@Getter
@Setter
public class ApiKeyRequest {

    private String name;

    private String scopes;

    private Integer expiresInDays;



    public List<String> getScopeList() {
        if (scopes == null || scopes.isBlank()) return List.of();
        return Arrays.asList(scopes.split(","));
    }
}
