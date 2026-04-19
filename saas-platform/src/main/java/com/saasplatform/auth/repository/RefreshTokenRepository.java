package com.saasplatform.auth.repository;

import com.saasplatform.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , UUID> {

    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(UUID userId);

}

