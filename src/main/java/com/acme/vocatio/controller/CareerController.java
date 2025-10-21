package com.acme.vocatio.controller;

import com.acme.vocatio.dto.career.CareerDetailDto;
import com.acme.vocatio.dto.career.CareerPageResponse;
import com.acme.vocatio.service.CareerService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/**
 * Controlador para explorar las carreras disponibles.
 * Casos de uso: M3-01 (listar) y M3-02 (filtrar).
 */
@RestController
@RequestMapping("/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    /** Endpoint: GET /api/careers - Lista paginada (M3-01). */
    @GetMapping
    public ResponseEntity<CareerPageResponse> listCareers(
            Pageable pageable,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false, name = "type") String tipoFormacion) {

        CareerPageResponse response = careerService.listCareers(pageable, area, duration, tipoFormacion);
        return ResponseEntity.ok(response);
    }

    /** Endpoint: GET /api/careers/{id} - Detalle ampliado (M3-03). */
    @GetMapping("/{id}")
    public ResponseEntity<CareerDetailDto> getCareerDetail(@PathVariable Long id) {
        CareerDetailDto detail = careerService.getCareerDetail(id);
        return ResponseEntity.ok(detail);
    }
}
