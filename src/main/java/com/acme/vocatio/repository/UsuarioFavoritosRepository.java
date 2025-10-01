package com.acme.vocatio.repository;

import com.acme.vocatio.model.UsuarioFavoritos;
import com.acme.vocatio.model.UsuarioFavoritosId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioFavoritosRepository extends JpaRepository<UsuarioFavoritos, UsuarioFavoritosId> {

    List<UsuarioFavoritos> findByIdUsuario(Long idUsuario);

    @Query("SELECT uf FROM UsuarioFavoritos uf JOIN FETCH uf.carrera WHERE uf.idUsuario = :idUsuario")
    List<UsuarioFavoritos> findFavoritosByUsuarioConCarrera(@Param("idUsuario") Long idUsuario);

    boolean existsByIdUsuarioAndIdCarrera(Long idUsuario, Long idCarrera);

    void deleteByIdUsuarioAndIdCarrera(Long idUsuario, Long idCarrera);
}