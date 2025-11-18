package com.acme.vocatio.dto.profile;

/** Respuesta simple al actualizar el perfil. */
public record ProfileUpdateResponse(String message, ProfileDto profile) {
}