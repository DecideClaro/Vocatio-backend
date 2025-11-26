package com.acme.vocatio.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record DeepSeekAnswerRequest(
        @NotBlank(message = "questionId es obligatorio") String questionId,
        @NotBlank(message = "optionId es obligatorio") String optionId,
        @NotBlank(message = "value es obligatorio") String value) {}
