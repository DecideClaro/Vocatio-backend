package com.acme.vocatio.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales necesarias para iniciar sesión")
public record LoginRequest(
        @Schema(example = "ada.lovelace@example.com")
                @NotBlank(message = "El email es obligatorio")
                @Email(message = "Ingresa un email válido")
                String email,
        @Schema(example = "ClaveSegura1")
                @NotBlank(message = "La contraseña es obligatoria") String password,
        @Schema(description = "Recuerda la sesión si es verdadero", example = "true")
                Boolean rememberMe) {

    //public boolean rememberMe() {
    //    return Boolean.TRUE.equals(rememberMe);
    //}
}
