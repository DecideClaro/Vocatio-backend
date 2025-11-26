package com.acme.vocatio.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DeepSeekRequest(
        @NotEmpty(message = "answers es obligatorio y no puede estar vacio")
                List<@Valid DeepSeekAnswerRequest> answers,

        @Size(max = 400, message = "notes no puede exceder 400 caracteres")
                String notes) {}
