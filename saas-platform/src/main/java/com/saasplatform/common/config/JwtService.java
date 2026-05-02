package com.saasplatform.common.config;

import com.saasplatform.user.entity.User;
import com.saasplatform.common.context.TenantInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    // Generate Access Token
    public String generateAccessToken(User user, TenantInfo tenant) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tenantId", tenant.id().toString())
                .claim("slug", tenant.slug())
                .claim("plan", tenant.plan())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    // Validate Token
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token); // will throw if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extract Claims
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())             // not setSigningKey()
                .build()
                .parseSignedClaims(token)               // not parseClaimsJws()
                .getPayload();                           // not getBody()
    }

    // Signing Key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}