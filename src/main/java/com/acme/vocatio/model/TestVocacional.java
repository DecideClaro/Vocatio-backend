package com.acme.vocatio.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "testvocacional")
@Data
@NoArgsConstructor
public class TestVocacional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test")
    private Integer id;

    private String nombre;
    private String descripcion;

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY)
    @JsonManagedReference // Evita la recursión infinita al serializar a JSON
    private List<Question> questions;
}
