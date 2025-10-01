package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usuariofavoritos")
@Getter
@Setter
@NoArgsConstructor
@IdClass(UsuarioFavoritosId.class)
public class UsuarioFavoritos {

    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Id
    @Column(name = "id_carrera")
    private Long idCarrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", insertable = false, updatable = false)
    private Carrera carrera;

    @Column(name = "fecha_agregado", nullable = false)
    private Instant fechaAgregado = Instant.now();
}