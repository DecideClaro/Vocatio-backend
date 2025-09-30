package com.acme.vocatio.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UsuarioFavoritosId implements Serializable {
    private Long idUsuario;
    private Long idCarrera;
}