package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "testvocacional")
@Getter
@Setter
@NoArgsConstructor
public class TestVocacional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test_vocacional")
    private Long id;

    @Column(name = "nombre_test", nullable = false)
    private String nombreTest;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "version_test", length = 50)
    private String versionTest;
}