package com.acme.vocatio.controller;

import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para explorar las carreras disponibles.
 * Caso de uso: M3-01 - Listar fichas de carrera.
 */
@RestController
@RequestMapping("/api/careers") // convención: agregar /api para endpoints REST
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    /**
     * Endpoint: GET /api/careers
     * Devuelve una lista paginada de fichas de carrera (nombre, duración, modalidad, descripción).
     */
    @GetMapping
    public ResponseEntity<Page<CareerListDto>> listCareers(Pageable pageable) {
        Page<CareerListDto> careers = careerService.listCareers(pageable);

        if (careers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(careers);
    }

}
