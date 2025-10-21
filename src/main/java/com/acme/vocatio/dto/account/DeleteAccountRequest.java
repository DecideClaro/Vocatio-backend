package com.acme.vocatio.dto.account;

import jakarta.validation.constraints.NotBlank;

/** Solicitud para confirmar la eliminación definitiva de la cuenta. */
public record DeleteAccountRequest(
        @NotBlank(message = "Debes escribir \"ELIMINAR\" para confirmar") String confirmation,
        @NotBlank(message = "La contraseña es obligatoria") String currentPassword) {}
