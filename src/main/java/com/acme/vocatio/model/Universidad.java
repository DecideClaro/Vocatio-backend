package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "universidad")
@Getter
@Setter
@NoArgsConstructor
public class Universidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_universidad")
    private Long id;

    @Column(name = "nombre_universidad", nullable = false)
    private String nombreUniversidad;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "sitio_web")
    private String sitioWeb;
}