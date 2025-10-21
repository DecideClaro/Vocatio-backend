package com.acme.vocatio.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solicitud para actualizar la contraseña actual. */
public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria") String currentPassword,
        @NotBlank(message = "La nueva contraseña es obligatoria")
                @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
                @Pattern(regexp = ".*[A-Z].*", message = "La contraseña debe incluir al menos una letra mayúscula")
                @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe incluir al menos un número")
                String newPassword) {}
