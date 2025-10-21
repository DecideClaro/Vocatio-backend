package com.acme.vocatio.dto.account;

/** Respuesta al eliminar una cuenta y sus datos asociados. */
public record DeleteAccountResponse(String message, boolean pendingDeletion) {}
