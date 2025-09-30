package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carrera")
@Getter
@Setter
@NoArgsConstructor
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "descripcion_corta", nullable = false, columnDefinition = "TEXT")
    private String descripcionCorta;

    @Column(name = "descripcion_larga", columnDefinition = "TEXT")
    private String descripcionLarga;

    @Column(name = "plan_de_estudios", columnDefinition = "TEXT")
    private String planDeEstudios;

    @Column(name = "salida_laboral", columnDefinition = "TEXT")
    private String salidaLaboral;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // Las relaciones las agregaremos después cuando todas las entidades estén creadas
}