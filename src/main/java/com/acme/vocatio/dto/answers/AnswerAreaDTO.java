package com.acme.vocatio.dto.answers;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTO para representar el puntaje de un área de interés
@Data
@AllArgsConstructor
public class AnswerAreaDTO {
    private String nombreArea;
    private int puntaje;
    private String descripcion;
}
