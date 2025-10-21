package com.acme.vocatio.dto.profile;

/** Respuesta al actualizar los datos personales. */
public record PersonalDataUpdateResponse(String message, ProfileDto profile) {}
