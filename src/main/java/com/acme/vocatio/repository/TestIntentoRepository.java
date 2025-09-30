package com.acme.vocatio.repository;

import com.acme.vocatio.model.TestIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestIntentoRepository extends JpaRepository<TestIntento, Long> {

    List<TestIntento> findByUsuarioIdOrderByFechaInicioDesc(Long usuarioId);

    Optional<TestIntento> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    @Query("SELECT ti FROM TestIntento ti WHERE ti.usuario.id = :usuarioId AND ti.esVisible = true " +
           "ORDER BY ti.fechaInicio DESC")
    List<TestIntento> findIntentosVisiblesByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT ti FROM TestIntento ti WHERE ti.usuario.id = :usuarioId AND ti.estado = 'completado' " +
           "ORDER BY ti.fechaFinalizacion DESC")
    List<TestIntento> findIntentosCompletadosByUsuario(@Param("usuarioId") Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}