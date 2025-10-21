package com.acme.vocatio.dto.career;

import java.util.List;

/**
 * Respuesta paginada para el listado de carreras.
 */
public record CareerPageResponse(
        List<CareerListDto> careers,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String message
) {
}
