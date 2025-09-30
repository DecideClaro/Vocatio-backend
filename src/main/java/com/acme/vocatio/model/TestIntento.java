package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "testintento")
@Getter
@Setter
@NoArgsConstructor
public class TestIntento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_test_vocacional", nullable = false)
    private TestVocacional testVocacional;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio = Instant.now();

    @Column(name = "fecha_finalizacion")
    private Instant fechaFinalizacion;

    @Column(length = 50, nullable = false)
    private String estado = "en_progreso";

    @Column(name = "es_visible", nullable = false)
    private Boolean esVisible = true;
}