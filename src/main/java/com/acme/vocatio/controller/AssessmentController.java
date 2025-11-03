package com.acme.vocatio.controller;

import com.acme.vocatio.dto.assessment.*;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.UserRepository;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de evaluaciones vocacionales (Módulo 2).
 * Gestiona intentos, resultados y reportes del test vocacional.
 */
@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
@Tag(name = "Evaluaciones vocacionales", description = "Intentos, resultados y reportes del test")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
            summary = "Crea un nuevo intento de evaluación",
            description = "Inicializa un intento paginado con barra de progreso y opción de guardar más tarde.",
            requestBody = @RequestBody(required = false, description = "Metadatos opcionales para precargar estado."),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Intento creado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"id\": \"a1b2c3\",\n  \"status\": \"IN_PROGRESS\",\n  \"progress\": {\n    \"currentPage\": 1,\n    \"totalPages\": 10,\n    \"answeredQuestions\": 3\n  },\n  \"pages\": [\n    {\n      \"page\": 1,\n      \"questions\": [\n        {\n          \"id\": \"Q1\",\n          \"title\": \"Prefiero actividades al aire libre\",\n          \"required\": true,\n          \"options\": [\n            { \"id\": \"O1\", \"label\": \"Sí\" },\n            { \"id\": \"O2\", \"label\": \"No\" }\n          ]\n        }\n      ]\n    }\n  ],\n  \"features\": { \"allowSaveForLater\": true }\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}"))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Ya existe un intento en curso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Tienes un test pendiente\",\n  \"pendingAssessmentId\": \"a1b2c3\"\n}")))
            }
    )
    public ResponseEntity<AssessmentResponse> createAssessment(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            AssessmentResponse response = assessmentService.createAssessment(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{assessmentId}")
    @Operation(
            summary = "Guarda respuestas parciales",
            description = "Persiste respuestas de la página actual y valida preguntas obligatorias antes de avanzar.",
            requestBody = @RequestBody(required = true, description = "Listado de respuestas seleccionadas.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\n  \"answers\": [\n    { \"questionId\": \"Q1\", \"optionId\": \"O1\" },\n    { \"questionId\": \"Q2\", \"optionId\": \"O7\" }\n  ]\n}"))),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Respuestas guardadas",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"id\": \"a1b2c3\",\n  \"status\": \"IN_PROGRESS\",\n  \"answers\": [\n    { \"questionId\": \"Q1\", \"optionId\": \"O1\" }\n  ],\n  \"progress\": {\n    \"answeredQuestions\": 10,\n    \"totalQuestions\": 45\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Faltan preguntas obligatorias",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Completa las preguntas marcadas\",\n  \"errors\": { \"Q5\": [\"Esta pregunta es obligatoria\"] }\n}"))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Intento no encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Evaluación no encontrada\"\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<AssessmentResponse> saveProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId,
            @org.springframework.web.bind.annotation.RequestBody SaveAnswersRequest request
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            AssessmentResponse response = assessmentService.saveProgress(Long.parseLong(assessmentId), user, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{assessmentId}")
    @Operation(
            summary = "Recupera un intento en curso",
            description = "Devuelve respuestas previas, progreso y metadatos de accesibilidad para reanudar el test.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Intento encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"id\": \"a1b2c3\",\n  \"status\": \"IN_PROGRESS\",\n  \"answers\": [\n    { \"questionId\": \"Q1\", \"optionId\": \"O1\" }\n  ],\n  \"progress\": {\n    \"currentPage\": 2,\n    \"totalPages\": 10\n  },\n  \"metadata\": {\n    \"aria\": { \"nextButton\": \"Siguiente página\" }\n  }\n}"))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Intento no encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Evaluación no encontrada\"\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}"))),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Intento pertenece a otra persona",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Acceso denegado\"\n}")))
            }
    )
    public ResponseEntity<AssessmentResponse> getAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            AssessmentResponse response = assessmentService.getAssessment(Long.parseLong(assessmentId), user);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{assessmentId}/submit")
    @Operation(
            summary = "Envía un intento para cálculo de resultados",
            description = "Marca el intento como finalizado y dispara el proceso de scoring vocacional.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Scoring aceptado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"assessmentId\": \"a1b2c3\",\n  \"resultId\": \"r-789\",\n  \"status\": \"COMPLETED\"\n}"))),
                    @ApiResponse(
                            responseCode = "202",
                            description = "Resultado en cola",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"assessmentId\": \"a1b2c3\",\n  \"status\": \"SCORING\",\n  \"message\": \"El cálculo finalizará en segundos\"\n}"))),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Intento ya finalizado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"La evaluación ya fue enviada\",\n  \"resultId\": \"r-789\"\n}"))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Faltan respuestas obligatorias",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Completa todas las preguntas\"\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<SubmitAssessmentResponse> submitAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            SubmitAssessmentResponse response = assessmentService.submitAssessment(Long.parseLong(assessmentId), user);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{assessmentId}/result")
    @Operation(
            summary = "Consulta los resultados del test",
            description = "Devuelve puntajes por área, top 3 predominantes y carreras sugeridas.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resultados disponibles",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"assessmentId\": \"a1b2c3\",\n  \"topAreas\": [\n    { \"code\": \"I\", \"name\": \"Investigativo\", \"score\": 18, \"summary\": \"Te gusta analizar\" },\n    { \"code\": \"A\", \"name\": \"Artístico\", \"score\": 15 },\n    { \"code\": \"S\", \"name\": \"Social\", \"score\": 12 }\n  ],\n  \"suggestedCareers\": [\n    { \"id\": 101, \"name\": \"Ingeniería de datos\" },\n    { \"id\": 202, \"name\": \"Diseño multimedia\" }\n  ],\n  \"chart\": { \"type\": \"radar\", \"series\": [18, 15, 12, 9, 6, 4] }\n}"))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Resultados no encontrados",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Resultado no disponible\"\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}"))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error de cálculo",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Intenta nuevamente, ocurrió un error al calcular\"\n}")))
            }
    )
    public ResponseEntity<AssessmentResultResponse> getResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            AssessmentResultResponse response = assessmentService.getResult(Long.parseLong(assessmentId), user);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @Operation(
            summary = "Lista intentos anteriores",
            description = "Devuelve historial con fecha, estado y enlaces para ver detalle o descargar informe.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Historial recuperado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"items\": [\n    {\n      \"id\": \"a1b2c3\",\n      \"status\": \"COMPLETED\",\n      \"completedAt\": \"2024-05-01T10:15:00Z\",\n      \"topAreas\": [\n        { \"code\": \"I\", \"score\": 18 },\n        { \"code\": \"A\", \"score\": 15 }\n      ],\n      \"links\": {\n        \"detail\": \"/assessments/a1b2c3\",\n        \"report\": \"/assessments/a1b2c3/report.pdf\"\n      }\n    }\n  ]\n}"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}")))
            }
    )
    public ResponseEntity<AssessmentListResponse> listAssessments(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        AssessmentListResponse response = assessmentService.listAssessments(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assessmentId}/report.pdf")
    @Operation(
            summary = "Descarga el informe PDF",
            description = "Entrega un PDF con datos del perfil, puntajes y materiales recomendados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF generado",
                            content = @Content(mediaType = "application/pdf",
                                    schema = @Schema(type = "string", format = "binary"))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}"))),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Intento pertenece a otra persona",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Acceso denegado\"\n}"))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Intento no encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Evaluación no encontrada\"\n}")))
            }
    )
    public ResponseEntity<Map<String, String>> downloadReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        // PDF generation is not implemented yet - return 501
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Generacion de PDF pendiente de implementacion"));
    }

    @DeleteMapping("/{assessmentId}")
    @Operation(
            summary = "Elimina una evaluación previa",
            description = "Borra el intento del historial y registra un log interno de auditoría.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Evaluación eliminada",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Evaluación eliminada\"\n}"))),
                    @ApiResponse(
                            responseCode = "204",
                            description = "Evaluación eliminada sin contenido"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Sesión inválida",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"No autorizado\"\n}"))),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Intento de otra persona",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(example = "{\n  \"message\": \"Acceso denegado\"\n}")))
            }
    )
    public ResponseEntity<Map<String, String>> deleteAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            assessmentService.deleteAssessment(Long.parseLong(assessmentId), user);
            return ResponseEntity.ok(Map.of("message", "Evaluacion eliminada"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
