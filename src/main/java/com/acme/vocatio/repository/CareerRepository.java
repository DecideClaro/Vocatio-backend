package com.acme.vocatio.repository;

import com.acme.vocatio.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio JPA para la entidad Career. */
@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
    // Para M3-01 no necesitamos consultas adicionales todavía
}
