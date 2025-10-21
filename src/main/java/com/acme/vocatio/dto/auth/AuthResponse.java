package com.acme.vocatio.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Respuesta estándar para operaciones de autenticación")
public record AuthResponse(
        @Schema(example = "Registro exitoso") String message,
        UserSummary user,
        TokenBundle tokens) {

    @Schema(description = "Resumen de la persona usuaria autenticada")
    public record UserSummary(
            @Schema(example = "42") Long id,
            @Schema(example = "ada.lovelace@example.com") String email) {}

    @Schema(description = "Tokens emitidos tras la autenticación")
    public record TokenBundle(
            @Schema(example = "Bearer") String tokenType,
            @Schema(description = "JWT de acceso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
            @Schema(description = "Fecha de expiración del access token en ISO-8601", example = "2025-03-01T12:34:56Z")
                    Instant accessTokenExpiresAt,
            @Schema(description = "Token para renovar la sesión", example = "dXNlcjoxMjM0NTY=") String refreshToken,
            @Schema(description = "Fecha de expiración del refresh token en ISO-8601", example = "2025-03-15T12:34:56Z")
                    Instant refreshTokenExpiresAt) {}
}
