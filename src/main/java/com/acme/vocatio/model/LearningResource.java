package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un recurso de aprendizaje.
 */
@Entity
@Table(name = "recursoaprendizaje")
@Getter
@Setter
@NoArgsConstructor
public class LearningResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recurso")
    private Long id;

    @Column(name = "id_carrera", nullable = false)
    private Long careerId;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "url_recurso", nullable = false, length = 255)
    private String urlRecurso;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "id_area_interes")
    private Long areaInteresId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false)
    private TipoRecurso tipoRecurso;

    @Column(name = "archivo_pdf", length = 500)
    private String archivoPdf;

    @Column(name = "url_valida")
    private Boolean urlValida;
}

