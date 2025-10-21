package com.acme.vocatio.service;

import com.acme.vocatio.model.RefreshToken;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 48;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public record RefreshTokenPayload(String token, Instant expiresAt) {}

    @Transactional
    public RefreshTokenPayload create(User user, Duration ttl) {
        String rawToken = generateToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(passwordEncoder.encode(rawToken));
        refreshToken.setExpiresAt(Instant.now().plus(ttl));
        refreshTokenRepository.save(refreshToken);
        return new RefreshTokenPayload(rawToken, refreshToken.getExpiresAt());
    }

    @Transactional
    public void revokeActiveTokens(User user) {
        refreshTokenRepository.revokeTokensForUser(user);
    }

    @Transactional
    public void deleteAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
