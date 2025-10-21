package com.acme.vocatio.controller;

import com.acme.vocatio.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Documentación de los flujos del módulo 2 (evaluaciones vocacionales).
 *
 * <p>Las implementaciones concretas aún están en progreso, pero se registran los contratos
 * esperados para facilitar el trabajo cruzado entre equipos FE/QA. Cada endpoint responde 501
 * (Not Implemented) hasta que el servicio correspondiente esté disponible.</p>
 */
@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
@Tag(name = "Evaluaciones vocacionales", description = "Intentos, resultados y reportes del test")
public class AssessmentController {

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
    public ResponseEntity<Void> createAssessment(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> saveProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> getAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> submitAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> getResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> listAssessments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> downloadReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
    public ResponseEntity<Void> deleteAssessment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Identificador del intento") @PathVariable String assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
