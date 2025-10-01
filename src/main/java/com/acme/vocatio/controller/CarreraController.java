package com.acme.vocatio.controller;

import com.acme.vocatio.model.Carrera;
import com.acme.vocatio.service.CarreraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {

    private final CarreraService carreraService;

    @GetMapping
    public ResponseEntity<List<Carrera>> obtenerTodasLasCarreras(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "busqueda", required = false) String busqueda) {
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            List<Carrera> carreras = carreraService.buscarCarreras(busqueda.trim());
            return ResponseEntity.ok(carreras);
        }

        if (size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Carrera> carrerasPage = carreraService.obtenerCarrerasPaginadas(pageable);
            return ResponseEntity.ok(carrerasPage.getContent());
        }

        List<Carrera> carreras = carreraService.obtenerTodasLasCarreras();
        return ResponseEntity.ok(carreras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrera> obtenerCarreraPorId(@PathVariable Long id) {
        return carreraService.obtenerCarreraPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Carrera> crearCarrera(@RequestBody Carrera carrera) {
        try {
            Carrera nuevaCarrera = carreraService.guardarCarrera(carrera);
            return ResponseEntity.ok(nuevaCarrera);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carrera> actualizarCarrera(
            @PathVariable Long id, 
            @RequestBody Carrera carrera) {
        
        if (!carreraService.obtenerCarreraPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        carrera.setId(id);
        Carrera carreraActualizada = carreraService.guardarCarrera(carrera);
        return ResponseEntity.ok(carreraActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrera(@PathVariable Long id) {
        if (!carreraService.obtenerCarreraPorId(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        carreraService.eliminarCarrera(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Long> contarCarreras() {
        long total = carreraService.contarCarreras();
        return ResponseEntity.ok(total);
    }
}