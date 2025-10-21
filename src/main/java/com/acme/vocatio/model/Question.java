package com.acme.vocatio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "pregunta")
@Data
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer id;

    @Column(name = "texto_pregunta")
    private String textoPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_test")
    @JsonBackReference
    private TestVocacional test;

    @OneToMany(mappedBy = "pregunta", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Option> opciones;
}