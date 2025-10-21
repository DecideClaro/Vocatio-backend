package com.acme.vocatio.dto.career;

import java.util.List;

/** DTO para la ficha detallada de una carrera (M3-03). */
public record CareerDetailDto(
        Long id,
        String nombre,
        String duracion,
        String modalidad,
        String planEstudiosBasico,
        String perfilEgreso,
        List<String> instituciones
) {
}
