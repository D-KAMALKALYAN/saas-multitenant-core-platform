package com.saasplatform.auth.dto;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        String tokenType,
        long   expiresIn
) {}

