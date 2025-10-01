package com.acme.vocatio.service;

import com.acme.vocatio.model.Carrera;
import com.acme.vocatio.repository.CarreraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public List<Carrera> obtenerTodasLasCarreras() {
        return carreraRepository.findAll();
    }

    public Page<Carrera> obtenerCarrerasPaginadas(Pageable pageable) {
        return carreraRepository.findAll(pageable);
    }

    public Optional<Carrera> obtenerCarreraPorId(Long id) {
        return carreraRepository.findById(id);
    }

    public List<Carrera> buscarCarreras(String busqueda) {
        return carreraRepository.buscarPorNombreODescripcion(busqueda);
    }

    public Carrera guardarCarrera(Carrera carrera) {
        return carreraRepository.save(carrera);
    }

    public void eliminarCarrera(Long id) {
        carreraRepository.deleteById(id);
    }

    public long contarCarreras() {
        return carreraRepository.contarTodasLasCarreras();
    }

    public boolean existeCarrera(String nombre) {
        return carreraRepository.findByNombreIgnoreCase(nombre).isPresent();
    }
}