package com.acme.vocatio.dto.answers;

import lombok.AllArgsConstructor;
import lombok.Data;

// Data Transfer Object para recibir las respuestas del usuario
@Data
@AllArgsConstructor
public class AnswerDTO {
    private Integer idPregunta;
    private Integer idOpcionSeleccionada;
}
