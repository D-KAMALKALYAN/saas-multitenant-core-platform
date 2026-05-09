package com.saasplatform.auth.controller;

import com.saasplatform.auth.dto.AuthTokens;
import com.saasplatform.auth.dto.LoginRequest;
import com.saasplatform.auth.service.AuthService;
import com.saasplatform.common.response.StandardApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication API",
        description = "APIs for authentication, token refresh, and logout"
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "User login",
            description = "Authenticates the user using email and password " +
                    "and returns access and refresh tokens."
    )
    @PostMapping("/login")
    public ResponseEntity<StandardApiResponse<AuthTokens>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(request));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token and refresh token " +
                    "using a valid refresh token."
    )
    @PostMapping("/refresh")
    public ResponseEntity<StandardApiResponse<AuthTokens>> refresh(
            @Valid @RequestParam String refreshToken
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refresh(refreshToken));
    }

    @Operation(
            summary = "Logout user",
            description = "Logs out the user by invalidating the provided refresh token."
    )
    @PostMapping("/logout")
    public ResponseEntity<StandardApiResponse<Void>> logout(
            @RequestParam String refreshToken
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.logout(refreshToken));
    }
}