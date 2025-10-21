package com.acme.vocatio.dto.learningresource;

import com.acme.vocatio.model.TipoRecurso;

/**
 * DTO para mostrar información de un recurso de aprendizaje.
 */
public record LearningResourceDto(
    Long id,
    Long careerId,
    String titulo,
    String urlRecurso,
    String descripcion,
    Integer duracionMinutos,
    Long areaInteresId,
    boolean isSaved,
    TipoRecurso tipoRecurso,
    boolean esDescargable,
    boolean esEnlaceExterno,
    String archivoPdf,
    Boolean urlValida
) {
}

