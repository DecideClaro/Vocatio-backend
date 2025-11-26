package com.acme.vocatio.service;

import com.acme.vocatio.config.DeepSeekProperties;
import com.acme.vocatio.dto.ai.DeepSeekAnswerRequest;
import com.acme.vocatio.dto.ai.DeepSeekRequest;
import com.acme.vocatio.dto.ai.DeepSeekResponse;
import com.acme.vocatio.exception.AiServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekService {

    private static final String SYSTEM_PROMPT =
            "Actua como orientador vocacional. Devuelve unicamente un JSON con los campos "
                    + "\"mbtiProfile\" (string), \"suggestedCareers\" (arreglo de strings), "
                    + "\"qualities\" (arreglo de strings) y \"profileSummary\" (parrafo corto). "
                    + "No incluyas texto adicional.";

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public DeepSeekResponse fetchInsights(Long userId, DeepSeekRequest request) {
        if (request == null || request.answers() == null || request.answers().isEmpty()) {
            throw new AiServiceException(HttpStatus.BAD_REQUEST, "answers es obligatorio y no puede estar vacio");
        }

        String sanitizedNotes = sanitizeNotes(request.notes());
        String prompt = buildPrompt(request.answers(), sanitizedNotes);
        String payload = buildPayload(prompt);
        String rawResponse = executeWithRetries(userId, payload);
        return parseResponse(rawResponse);
    }

    private String sanitizeNotes(String notes) {
        if (!StringUtils.hasText(notes)) {
            return null;
        }
        String cleaned = Jsoup.clean(notes.trim(), Safelist.none());
        return cleaned.isBlank() ? null : cleaned;
    }

    private String buildPrompt(List<DeepSeekAnswerRequest> answers, String sanitizedNotes) {
        String answersBlock = answers.stream()
                .filter(Objects::nonNull)
                .map(answer -> "- Pregunta " + safeTrim(answer.questionId()) + ": " + safeTrim(answer.value()))
                .collect(Collectors.joining("\n"));

        StringBuilder builder = new StringBuilder();
        builder.append("Resumes los hallazgos vocacionales a partir de respuestas de un test.\n");
        builder.append("Respuestas del usuario:\n").append(answersBlock).append("\n");
        if (sanitizedNotes != null) {
            builder.append("Notas adicionales: ").append(sanitizedNotes).append("\n");
        }
        builder.append("Entrega solo un JSON valido con la estructura solicitada. ");
        builder.append("Incluye siempre el arreglo qualities con 3 a 5 adjetivos en espanol. ");
        builder.append("No incluyas bloques de codigo ni explicaciones.");
        return builder.toString();
    }

    private String buildPayload(String prompt) {
        Map<String, Object> payload = Map.of(
                "model", properties.getModel(),
                "messages",
                List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.4);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AiServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error construyendo la solicitud de IA", ex);
        }
    }

    private String executeWithRetries(Long userId, String payload) {
        String apiKey = properties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            log.error("DeepSeek API key no configurada");
            throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "IA no disponible, intenta luego");
        }

        URI uri = URI.create(properties.getApiUrl());
        Duration timeout = properties.getTimeout();
        HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        int attempts = properties.getMaxAttempts();
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }

                log.warn(
                        "DeepSeek respondio {} en el intento {} para el usuario {}",
                        response.statusCode(),
                        attempt,
                        userId);

                if (shouldRetry(response.statusCode()) && attempt < attempts) {
                    backoff(attempt);
                    continue;
                }

                throw new AiServiceException(HttpStatus.BAD_GATEWAY, "IA no disponible, intenta luego");
            } catch (IOException ex) {
                log.warn(
                        "Error de IO al invocar DeepSeek (intento {} de {}) para el usuario {}",
                        attempt,
                        attempts,
                        userId,
                        ex);
                if (attempt >= attempts) {
                    throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "IA no disponible, intenta luego", ex);
                }
                backoff(attempt);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "IA no disponible, intenta luego", ex);
            }
        }

        throw new AiServiceException(HttpStatus.SERVICE_UNAVAILABLE, "IA no disponible, intenta luego");
    }

    private boolean shouldRetry(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }

    private void backoff(int attempt) {
        try {
            TimeUnit.MILLISECONDS.sleep(250L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private DeepSeekResponse parseResponse(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode payloadNode = resolvePayloadNode(root);

            String mbti = textValue(payloadNode, "mbtiProfile");
            String summary = textValue(payloadNode, "profileSummary");
            List<String> careers = extractStrings(payloadNode.path("suggestedCareers"));
            List<String> qualities = extractStrings(payloadNode.path("qualities"));

            if (!StringUtils.hasText(mbti) || !StringUtils.hasText(summary)) {
                throw new AiServiceException(HttpStatus.BAD_GATEWAY, "IA no disponible, intenta luego");
            }

            return new DeepSeekResponse(mbti.trim(), careers, qualities, summary.trim());
        } catch (JsonProcessingException ex) {
            log.warn("No se pudo parsear la respuesta de DeepSeek", ex);
            throw new AiServiceException(HttpStatus.BAD_GATEWAY, "IA no disponible, intenta luego", ex);
        }
    }

    private JsonNode resolvePayloadNode(JsonNode root) throws JsonProcessingException {
        if (root.hasNonNull("mbtiProfile")) {
            return root;
        }

        String content = extractContent(root);
        if (!StringUtils.hasText(content)) {
            throw new AiServiceException(HttpStatus.BAD_GATEWAY, "IA no disponible, intenta luego");
        }

        String cleaned = stripCodeFences(content);
        return objectMapper.readTree(cleaned);
    }

    private String extractContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode messageContent = firstChoice.path("message").path("content");
            if (messageContent.isTextual()) {
                return messageContent.asText();
            }
            JsonNode textContent = firstChoice.path("text");
            if (textContent.isTextual()) {
                return textContent.asText();
            }
        }
        return null;
    }

    private String stripCodeFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineBreak >= 0 && lastFence > firstLineBreak) {
                return trimmed.substring(firstLineBreak + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode target = node.path(fieldName);
        if (target.isTextual()) {
            return target.asText();
        }
        return null;
    }

    private List<String> extractStrings(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return List.of();
        }

        if (!node.isArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        node.forEach(element -> {
            if (element != null && element.isTextual()) {
                String value = element.asText().trim();
                if (!value.isEmpty()) {
                    values.add(value);
                }
            }
        });

        return values.isEmpty() ? List.of() : List.copyOf(values);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
