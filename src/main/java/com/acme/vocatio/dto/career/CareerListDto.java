package com.acme.vocatio.dto.career;

/**
 * DTO para representar la informacion basica de una carrera al listar.
 * Caso de uso: M3-01.
 */
public record CareerListDto(
        Long id,
        String nombre,
        String duracion,
        String modalidad,
        String descripcion
) {}
