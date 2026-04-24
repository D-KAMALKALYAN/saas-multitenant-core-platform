package com.saasplatform.auth.controller;

import com.saasplatform.auth.repository.RefreshTokenRepository;
import com.saasplatform.auth.dto.AuthTokens;
import com.saasplatform.auth.service.AuthService;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<StandardApiResponse<AuthTokens>> login(@Valid @RequestBody LoginRequest request){
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(authService.login(request));
    }

    @PostMapping("/refresh")
    public  ResponseEntity<StandardApiResponse<AuthTokens>> refresh(@Valid @RequestParam String refreshToken){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardApiResponse<Void>> logout(@RequestParam String refreshToken){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.logout(refreshToken));
    }
}
