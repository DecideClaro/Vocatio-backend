package com.acme.vocatio.repository;

import com.acme.vocatio.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    List<Carrera> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT c FROM Carrera c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "OR LOWER(c.descripcionCorta) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Carrera> buscarPorNombreODescripcion(@Param("busqueda") String busqueda);

    Optional<Carrera> findByNombreIgnoreCase(String nombre);

    @Query("SELECT COUNT(c) FROM Carrera c")
    long contarTodasLasCarreras();
}