package com.acme.vocatio.controller;

import com.acme.vocatio.dto.account.DeleteAccountRequest;
import com.acme.vocatio.dto.account.DeleteAccountResponse;
import com.acme.vocatio.dto.profile.PersonalDataUpdateRequest;
import com.acme.vocatio.dto.profile.PersonalDataUpdateResponse;
import com.acme.vocatio.dto.profile.ProfileDto;
import com.acme.vocatio.dto.profile.ProfileUpdateRequest;
import com.acme.vocatio.dto.profile.ProfileUpdateResponse;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.UserAccountService;
import com.acme.vocatio.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints del perfil del usuario autenticado. */
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Tag(name = "Perfil de usuario", description = "Gestión del perfil y la cuenta de la persona autenticada")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserAccountService userAccountService;

    /**
     * Recupera el perfil del usuario logueado.
     */
    @GetMapping
    @Operation(
            summary = "Obtiene el perfil actual",
            description = "Retorna edad, grado académico, intereses y datos personales guardados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileDto.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<?>  getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                       @Valid @RequestBody ProfileUpdateRequest request) {
        try {
            var updatedProfile = userProfileService.updateCurrentUserProfile(principal.getUser().getId(), request);
            return ResponseEntity.ok(new ProfileUpdateResponse("Perfil actualizado", updatedProfile));
        } catch (Exception e) {
            e.printStackTrace(); // ver en logs
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage()));
        }
    }

    /** Actualiza edad, grado e intereses del usuario. */
    @PutMapping
    @Operation(
            summary = "Actualiza edad, grado e intereses",
            description = "Permite guardar los datos de perfil necesarios para personalizar recomendaciones.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil actualizado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileUpdateResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validaciones de edad o campos obligatorios",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Revisa los datos\",\n  \"errors\": {\n    \"age\": [\"Debe estar entre 13 y 30\"]\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<ProfileUpdateResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileDto updatedProfile = userProfileService.updateCurrentUserProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(new ProfileUpdateResponse("Perfil actualizado", updatedProfile));
    }

    /** Actualiza nombre y preferencias no sensibles. */
    @PatchMapping
    @Operation(
            summary = "Edita datos personales no sensibles",
            description = "Actualiza nombre y preferencias opcionales del perfil.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos personales actualizados",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PersonalDataUpdateResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validaciones de formato",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Revisa los datos\",\n  \"errors\": {\n    \"firstName\": [\"El nombre es obligatorio\"]\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<PersonalDataUpdateResponse> updatePersonalData(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PersonalDataUpdateRequest request) {
        ProfileDto updatedProfile = userProfileService.updatePersonalData(principal.getUser().getId(), request);
        return ResponseEntity.ok(new PersonalDataUpdateResponse("Datos personales actualizados", updatedProfile));
    }

    /** Elimina la cuenta y todos los datos asociados. */
    @DeleteMapping
    @Operation(
            summary = "Elimina la cuenta actual y todos los datos asociados",
            description = "Solicita la confirmación explícita escribiendo 'ELIMINAR' y validando la contraseña actual. "
                    + "Elimina perfil, evaluaciones y favoritos, además de revocar tokens de sesión.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Proceso de eliminación aceptado",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = DeleteAccountResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Confirmación o contraseña inválida",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(
                                        example = "{\n  \"message\": \"Debes escribir 'ELIMINAR' para confirmar\"\n}"))),
                @ApiResponse(
                        responseCode = "401",
                        description = "Sesión no autorizada",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(
                                        example = "{\n  \"message\": \"Credenciales inválidas\"\n}")))
            }
    )
    public ResponseEntity<DeleteAccountResponse> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody DeleteAccountRequest request) {
        DeleteAccountResponse response = userAccountService.deleteAccount(principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }
}
