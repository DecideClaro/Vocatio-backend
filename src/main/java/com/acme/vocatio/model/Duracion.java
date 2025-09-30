package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "duracion")
@Getter
@Setter
@NoArgsConstructor
public class Duracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duracion")
    private Long id;

    @Column(name = "rango_duracion", nullable = false, unique = true, length = 50)
    private String rangoDuracion;
}