package com.acme.vocatio.repository;

import com.acme.vocatio.model.TestVocacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TestVocacionalRepository extends JpaRepository<TestVocacional, Integer> {
    // Usamos un JOIN FETCH para cargar el test con todas sus preguntas y opciones en una sola consulta
    @Query("SELECT t FROM TestVocacional t LEFT JOIN FETCH t.questions p LEFT JOIN FETCH p.opciones WHERE t.id = :id")
    Optional<TestVocacional> findByIdWithDetails(Integer id);
}
