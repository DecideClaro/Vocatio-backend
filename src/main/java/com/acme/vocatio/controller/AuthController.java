package com.acme.vocatio.controller;

import com.acme.vocatio.dto.auth.AuthResponse;
import com.acme.vocatio.dto.auth.ChangePasswordRequest;
import com.acme.vocatio.dto.auth.ChangePasswordResponse;
import com.acme.vocatio.dto.auth.LoginRequest;
import com.acme.vocatio.dto.auth.RegisterRequest;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión de personas usuarias")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Registra una nueva cuenta",
            description = "Crea una cuenta con email y contraseña válidos. Si el registro es exitoso se inicia sesión de forma automática",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Registro exitoso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Email o contraseña inválidos",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            description = "Detalle de reglas de validación incumplidas",
                                            example = "{\n  \"message\": \"Revise los datos enviados\",\n  \"errors\": {\n    \"email\": [\"Ingresa un email válido\"],\n    \"password\": [\"La contraseña debe tener al menos 8 caracteres\"]\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email ya registrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            description = "Mensaje y sugerencias cuando el email ya está en uso",
                                            example = "{\n  \"message\": \"El email ya está registrado\",\n  \"suggestions\": [\"Inicia sesión con tu cuenta\", \"Recupera tu contraseña si la olvidaste\"]\n}")))
            })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Inicia sesión con una cuenta existente",
            description = "Autentica a la persona usuaria usando email y contraseña. Entrega tokens de acceso y refresh",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Autenticación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Email o contraseña faltante o inválida",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            description = "Detalle de validaciones fallidas",
                                            example = "{\n  \"message\": \"Revise los datos enviados\",\n  \"errors\": {\n    \"email\": [\"Ingresa un email válido\"],\n    \"password\": [\"La contraseña es obligatoria\"]\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Credenciales inválidas",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            description = "Mensaje genérico cuando el email o la contraseña no coinciden",
                                            example = "{\n  \"message\": \"Credenciales inválidas\"\n}")))
            })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Cambia la contraseña de la cuenta actual",
            description = "Requiere la contraseña vigente y una nueva que cumpla la política de seguridad.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contraseña actualizada",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ChangePasswordResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Reglas de contraseña incumplidas",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Revisa los datos enviados\",\n  \"errors\": {\n    \"newPassword\": [\"Debe tener al menos 8 caracteres, 1 mayúscula y 1 número\"]\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Contraseña actual incorrecta o sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Credenciales inválidas\"\n}")))
            }
    )
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUser().getId(), request);
        return ResponseEntity.ok(new ChangePasswordResponse("Contraseña actualizada. Inicia sesión nuevamente.", true));
    }
}
