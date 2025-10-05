package com.acme.vocatio.service;

import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.model.Career;
import com.acme.vocatio.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio para operaciones de carreras. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerService {

    private final CareerRepository careerRepository;

    /** Devuelve una pagina de carreras mapeadas a CareerListDto. */
    public Page<CareerListDto> listCareers(Pageable pageable) {
        return careerRepository.findAll(pageable)
                .map(this::toDto);
    }

    /** Mapea entidad Career a DTO CareerListDto. */
    private CareerListDto toDto(Career career) {
        return new CareerListDto(
                career.getId(),
                career.getNombre(),
                career.getDuracion(),
                career.getModalidad(),
                career.getDescripcion()
        );
    }
}
