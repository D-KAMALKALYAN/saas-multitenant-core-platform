package com.saasplatform.auth.service;

import com.saasplatform.auth.dto.AuthTokens;
import com.saasplatform.auth.dto.LoginRequest;
import com.saasplatform.auth.entity.RefreshToken;
import com.saasplatform.auth.repository.RefreshTokenRepository;
import com.saasplatform.common.config.JwtService;
import com.saasplatform.common.context.TenantInfo;
import com.saasplatform.common.exception.InvalidCredentialsException;
import com.saasplatform.common.response.StandardApiResponse;
import com.saasplatform.tenant.entity.Tenant;
import com.saasplatform.tenant.repository.TenantRepository;
import com.saasplatform.tenant.service.TenantValidationService;
import com.saasplatform.user.entity.User;
import com.saasplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantValidationService tenantValidationService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    public StandardApiResponse<AuthTokens> login(LoginRequest request){

        //Validate Tenant
        Tenant tenant = tenantValidationService.getActiveTenantBySlug(request.getSlug());

        //Find user
        User user = userRepository
                .findByEmailAndTenantIdAndDeletedAtIsNull(request.getEmail() , tenant.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        //Check active
        if(!user.isActive()){
                throw new InvalidCredentialsException("Invalid email or password");
        }

        //Verify password
        if(!passwordEncoder.matches(request.getPassword() , user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        //Build TenantInfo for JWT generation
        TenantInfo tenantInfo = new TenantInfo(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                tenant.getPlan().name()
        );

        //Generate Access Token
        String accessToken = jwtService.generateAccessToken(user , tenantInfo);

        //Generate refresh token
        String refreshToken = UUID.randomUUID().toString();

        // Save refresh token
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tenantId(tenant.getId())
                .token(refreshToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        AuthTokens authTokens = new AuthTokens(accessToken , refreshToken , "Bearer",accessTokenExpiry / 1000);
        return StandardApiResponse.success("Login successful" , authTokens);
    }

    public StandardApiResponse<AuthTokens> refresh(String refreshToken){
        RefreshToken tokenEntity = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if(tokenEntity.isRevoked() ||
        tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = tokenEntity.getUser();


        Tenant tenant = tenantValidationService.getActiveTenantByTenantId(tokenEntity.getTenantId());

        //Build TenantInfo for JWT generation
        TenantInfo tenantInfo = new TenantInfo(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                tenant.getPlan().name()
        );





        String newAccessToken = jwtService.generateAccessToken(user, tenantInfo);

        AuthTokens authTokens = new  AuthTokens(
                newAccessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiry / 1000
        );

        return StandardApiResponse.success("Success" , authTokens);
    }

    public StandardApiResponse<Void> logout(String refreshToken){

        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });

        return StandardApiResponse.success("Logout Successful");

    }


}
