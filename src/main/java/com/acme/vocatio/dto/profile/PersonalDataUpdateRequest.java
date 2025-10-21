package com.acme.vocatio.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** Payload para actualizar nombre y preferencias públicas. */
public record PersonalDataUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
                @Size(max = 100, message = "El nombre debe tener máximo 100 caracteres")
                String name,
        Map<@NotBlank(message = "La clave de preferencia no puede estar vacía") String,
                @NotNull(message = "El valor de la preferencia es obligatorio") Boolean> preferences) {
}
