package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad JPA que representa una carrera. */
@Entity
@Table(name = "careers")
@Getter
@Setter
@NoArgsConstructor
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String duracion;

    @Column(nullable = false, length = 50)
    private String modalidad;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
