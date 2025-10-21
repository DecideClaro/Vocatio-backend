package com.acme.vocatio.repository;

import com.acme.vocatio.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Career.
 * Incluye métodos para filtros dinámicos según diferentes combinaciones de campos.
 */
@Repository
public interface CareerRepository extends JpaRepository<Career, Long>, JpaSpecificationExecutor<Career> {
}
