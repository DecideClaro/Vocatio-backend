package com.acme.vocatio.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solicitud para actualizar la contraseña actual. */
public record ChangePasswordRequest(
        @NotBlank(message = "La contrasena actual es obligatoria") String currentPassword,
        @NotBlank(message = "La nueva contrasena es obligatoria")
                @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
                @Pattern(regexp = ".*[A-Z].*", message = "La contrasena debe incluir al menos una letra mayuscula")
                @Pattern(regexp = ".*[0-9].*", message = "La contrasena debe incluir al menos un numero")
                String newPassword) {}
