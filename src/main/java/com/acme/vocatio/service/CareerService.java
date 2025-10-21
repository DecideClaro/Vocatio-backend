package com.acme.vocatio.service;

import com.acme.vocatio.dto.career.CareerDetailDto;
import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.dto.career.CareerPageResponse;
import com.acme.vocatio.model.Career;
import com.acme.vocatio.exception.CareerNotFoundException;
import com.acme.vocatio.repository.CareerRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Servicio para operaciones de carreras. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerService {

    private static final int MAX_PAGE_SIZE = 20;
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("nombre"));

    private final CareerRepository careerRepository;

    /** Devuelve una página de carreras mapeadas a CareerListDto (M3-01 & M3-02). */
    public CareerPageResponse listCareers(Pageable pageable, String area, String duracion, String tipoFormacion) {
        Pageable effectivePageable = resolvePageable(pageable);

        Page<CareerListDto> careers = careerRepository
                .findAll(buildSpecification(area, duracion, tipoFormacion), effectivePageable)
                .map(this::toListDto);

        String message = careers.hasContent() ? null : "No hay carreras disponibles en este momento.";

        return new CareerPageResponse(
                careers.getContent(),
                careers.getNumber(),
                careers.getSize(),
                careers.getTotalElements(),
                careers.getTotalPages(),
                message
        );
    }

    /** Devuelve el detalle completo de una carrera (M3-03). */
    public CareerDetailDto getCareerDetail(Long careerId) {
        Career career = careerRepository.findById(careerId).orElseThrow(() -> new CareerNotFoundException(careerId));
        return toDetailDto(career);
    }

    /** Mapea entidad Career a DTO CareerListDto. */
    private CareerListDto toListDto(Career career) {
        return new CareerListDto(
                career.getId(),
                career.getNombre(),
                career.getDescripcion(),
                career.getDuracion(),
                career.getModalidad(),
                career.getAreaInteres(),
                career.getTipoFormacion()
        );
    }

    /** Mapea entidad Career a DTO de detalle. */
    private CareerDetailDto toDetailDto(Career career) {
        List<String> institutions = career.getInstituciones()
                .stream()
                .map(institution -> institution.getNombre())
                .sorted(Comparator.naturalOrder())
                .toList();

        return new CareerDetailDto(
                career.getId(),
                career.getNombre(),
                career.getDuracion(),
                career.getModalidad(),
                career.getPlanEstudiosBasico(),
                career.getPerfilEgreso(),
                institutions
        );
    }

    private Specification<Career> buildSpecification(String area, String duracion, String tipoFormacion) {
        Specification<Career> spec = Specification.where(null);

        if (StringUtils.hasText(area)) {
            String normalizedArea = area.trim().toLowerCase();
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("areaInteres")), normalizedArea));
        }

        if (StringUtils.hasText(duracion)) {
            String normalizedDuration = duracion.trim().toLowerCase();
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("duracion")), "%" + normalizedDuration + "%"));
        }

        if (StringUtils.hasText(tipoFormacion)) {
            String normalizedType = tipoFormacion.trim().toLowerCase();
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("tipoFormacion")), normalizedType));
        }

        return spec;
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, MAX_PAGE_SIZE, DEFAULT_SORT);
        }

        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : DEFAULT_SORT;

        if (size == pageable.getPageSize() && pageNumber == pageable.getPageNumber() && sort.equals(pageable.getSort())) {
            return pageable;
        }

        return PageRequest.of(pageNumber, size, sort);
    }
}
