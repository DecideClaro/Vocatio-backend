package com.acme.vocatio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "opcion")
@Data
@NoArgsConstructor
public class Opcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Integer id;

    @Column(name = "texto_opcion")
    private String textoOpcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    @JsonBackReference
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER para tener el área de interés siempre disponible
    @JoinColumn(name = "id_area_interes")
    @JsonBackReference
    private AreaInterest areaInteres;
}
