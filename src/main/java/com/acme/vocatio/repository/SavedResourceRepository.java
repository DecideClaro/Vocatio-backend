package com.acme.vocatio.repository;

import com.acme.vocatio.model.SavedResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedResourceRepository extends JpaRepository<SavedResource, SavedResource.SavedResourceId> {

    /**
     * Encuentra todos los recursos guardados por un usuario.
     */
    @Query("SELECT sr FROM SavedResource sr WHERE sr.id.userId = :userId ORDER BY sr.fechaGuardado DESC")
    Page<SavedResource> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Verifica si un recurso está guardado por un usuario.
     */
    @Query("SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END FROM SavedResource sr " +
           "WHERE sr.id.userId = :userId AND sr.id.resourceId = :resourceId")
    boolean existsByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * Encuentra un recurso guardado específico.
     */
    @Query("SELECT sr FROM SavedResource sr WHERE sr.id.userId = :userId AND sr.id.resourceId = :resourceId")
    Optional<SavedResource> findByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    /**
     * Elimina un recurso guardado.
     */
    void deleteByIdUserIdAndIdResourceId(Long userId, Long resourceId);

    /**
     * Obtiene los IDs de recursos guardados por un usuario.
     */
    @Query("SELECT sr.id.resourceId FROM SavedResource sr WHERE sr.id.userId = :userId")
    List<Long> findResourceIdsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM SavedResource sr WHERE sr.id.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

