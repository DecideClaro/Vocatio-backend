package com.acme.vocatio.service;

import com.acme.vocatio.config.JwtProperties;
import com.acme.vocatio.dto.auth.AuthResponse;
import com.acme.vocatio.dto.auth.ChangePasswordRequest;
import com.acme.vocatio.dto.auth.LoginRequest;
import com.acme.vocatio.dto.auth.RegisterRequest;
import com.acme.vocatio.exception.DuplicateEmailException;
import com.acme.vocatio.exception.InvalidCredentialsException;
import com.acme.vocatio.exception.InvalidCurrentPasswordException;
import com.acme.vocatio.exception.InvalidPasswordChangeException;
import com.acme.vocatio.exception.PasswordChangeRateLimitException;
import com.acme.vocatio.exception.UserNotFoundException;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.UserRepository;
import com.acme.vocatio.security.JwtService;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordChangeRateLimiter passwordChangeRateLimiter;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("El email ya está registrado");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user = userRepository.save(user);

        return buildAuthResponse(user, request.rememberMe(), "Registro exitoso");
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (Exception ex) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        return buildAuthResponse(user, rememberMe, "Inicio de sesión exitoso");
    }

    private AuthResponse buildAuthResponse(User user, boolean rememberMe, String message) {
        String accessToken = jwtService.generateAccessToken(user);
        Duration refreshTtl = rememberMe ? jwtProperties.getRememberMeTtl() : jwtProperties.getRefreshTokenTtl();

        refreshTokenService.revokeActiveTokens(user);
        RefreshTokenService.RefreshTokenPayload refreshTokenPayload = refreshTokenService.create(user, refreshTtl);

        Instant accessExpiration = jwtService.extractExpiration(accessToken);

        AuthResponse.TokenBundle tokens = new AuthResponse.TokenBundle(
                "Bearer",
                accessToken,
                accessExpiration,
                refreshTokenPayload.token(),
                refreshTokenPayload.expiresAt());

        AuthResponse.UserSummary userSummary = new AuthResponse.UserSummary(user.getId(), user.getEmail());

        return new AuthResponse(message, userSummary, tokens);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (passwordChangeRateLimiter.isBlocked(userId)) {
            long retryAfter = Math.max(1, passwordChangeRateLimiter.getRemainingLockDuration(userId).getSeconds());
            String message = String.format(
                    "Has superado el límite de intentos. Intenta nuevamente en %d segundos.", retryAfter);
            throw new PasswordChangeRateLimitException(message, retryAfter);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            passwordChangeRateLimiter.recordFailure(userId);
            throw new InvalidCurrentPasswordException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            passwordChangeRateLimiter.recordFailure(userId);
            throw new InvalidPasswordChangeException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeActiveTokens(user);
        passwordChangeRateLimiter.reset(userId);
    }
}
