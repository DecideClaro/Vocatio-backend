package com.acme.vocatio.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload necesario para registrar una nueva cuenta")
public record RegisterRequest(
        @Schema(example = "ada.lovelace@example.com", description = "Correo electrónico válido y único")
                @NotBlank(message = "El email es obligatorio")
                @Email(message = "Ingresa un email válido")
                String email,
        @Schema(example = "ClaveSegura1", description = "Contraseña que cumple con las políticas de seguridad")
                @NotBlank(message = "La contraseña es obligatoria")
                @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
                @Pattern(regexp = ".*[A-Z].*", message = "La contraseña debe incluir al menos una letra mayúscula")
                @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe incluir al menos un número")
                String password,
        @Schema(
                description = "Indica si se debe recordar la sesión del usuario para extender la vigencia del refresh token",
                example = "true")
                boolean rememberMe) {

    //public boolean rememberMe() {
    //    return Boolean.TRUE.equals(rememberMe);
    //}
}
