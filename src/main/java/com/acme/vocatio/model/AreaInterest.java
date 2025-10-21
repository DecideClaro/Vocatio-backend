package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

@Data // <-- La anotación clave que genera getters, setters, etc.
@NoArgsConstructor
@Entity
@Table(name = "areainteres")
public class AreaInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area_interes")
    private Integer id;


    @Column(name = "nombre_area")
    private String nombreArea;

    private String descripcion;
}
