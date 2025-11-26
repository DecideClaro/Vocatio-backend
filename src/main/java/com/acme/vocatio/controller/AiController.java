package com.acme.vocatio.controller;

import com.acme.vocatio.dto.ai.DeepSeekRequest;
import com.acme.vocatio.dto.ai.DeepSeekResponse;
import com.acme.vocatio.exception.AiServiceException;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.DeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "IA", description = "Proxy controlado hacia DeepSeek")
@Slf4j
public class AiController {

    private final DeepSeekService deepSeekService;

    @PostMapping("/deepseek")
    @Operation(
            summary = "Genera perfil vocacional usando DeepSeek",
            description = "Proxy autenticado que enriquece las respuestas del test con IA",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil generado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DeepSeekResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud invalida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{ \"message\": \"answers es obligatorio\" }"))),
                    @ApiResponse(
                            responseCode = "502",
                            description = "Fallo al contactar DeepSeek",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{ \"message\": \"IA no disponible, intenta luego\" }"))),
                    @ApiResponse(
                            responseCode = "503",
                            description = "DeepSeek no disponible",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{ \"message\": \"IA no disponible, intenta luego\" }")))
            })
    public ResponseEntity<?> generateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeepSeekRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String message = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .map(FieldError::getDefaultMessage)
                    .orElse("Solicitud invalida");
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }

        try {
            DeepSeekResponse response = deepSeekService.fetchInsights(principal.getUserId(), request);
            return ResponseEntity.ok(response);
        } catch (AiServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado en /ai/deepseek para el usuario {}", principal.getUserId(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ocurrio un error al procesar la solicitud"));
        }
    }
}
