package com.acme.vocatio.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA que representa una carrera.
 * Mapeada a la tabla Carrera existente en la base de datos.
 */
@Entity
@Table(name = "Carrera")
@Getter
@Setter
@NoArgsConstructor
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Long id;

    @Column(name = "nombre_carrera", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion_detallada", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "perfil_requerido", columnDefinition = "TEXT")
    private String perfilRequerido;

    @Column(name = "plan_estudios_basico", columnDefinition = "TEXT")
    private String planEstudiosBasico;

    @Column(name = "perfil_egreso", columnDefinition = "TEXT")
    private String perfilEgreso;

    // Campos opcionales para filtros futuros
    @Column(length = 50)
    private String duracion;      // Ej: "3 años", "5 años"

    @Column(length = 50)
    private String modalidad;     // Ej: "Presencial", "Virtual"

    @Column(length = 100)
    private String areaInteres;   // Ej: "Ingeniería", "Salud"

    @Column(length = 50)
    private String tipoFormacion; // Ej: "Técnica", "Universitaria"

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "CarreraInstitucion",
            joinColumns = @JoinColumn(name = "id_carrera"),
            inverseJoinColumns = @JoinColumn(name = "id_institucion"))
    private Set<Institution> instituciones = new HashSet<>();
}
