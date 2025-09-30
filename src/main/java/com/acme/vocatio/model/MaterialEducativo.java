package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "materialeducativo")
@Getter
@Setter
@NoArgsConstructor
public class MaterialEducativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_material", nullable = false, length = 50)
    private String tipoMaterial;

    @Column(name = "url_contenido", nullable = false)
    private String urlContenido;

    @Column(name = "imagen_preview_url")
    private String imagenPreviewUrl;
}