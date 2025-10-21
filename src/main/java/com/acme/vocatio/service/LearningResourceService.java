package com.acme.vocatio.service;

import com.acme.vocatio.dto.learningresource.LearningResourceDto;
import com.acme.vocatio.dto.learningresource.LearningResourceResponse;
import com.acme.vocatio.model.LearningResource;
import com.acme.vocatio.model.SavedResource;
import com.acme.vocatio.repository.LearningResourceRepository;
import com.acme.vocatio.repository.SavedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private final LearningResourceRepository learningResourceRepository;
    private final SavedResourceRepository savedResourceRepository;

    /**
     * Obtiene recursos sugeridos por carrera.
     */
    @Transactional(readOnly = true)
    public LearningResourceResponse getResourcesByCareer(Long userId, Long careerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningResource> resourcePage = learningResourceRepository.findByCareerId(careerId, pageable);

        return buildResponse(resourcePage, userId, "Recursos encontrados para la carrera");
    }

    /**
     * Obtiene recursos sugeridos por área de interés.
     */
    @Transactional(readOnly = true)
    public LearningResourceResponse getResourcesByArea(Long userId, Long areaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningResource> resourcePage = learningResourceRepository.findByAreaInteresId(areaId, pageable);

        return buildResponse(resourcePage, userId, "Recursos encontrados para el área de interés");
    }

    /**
     * Obtiene recursos sugeridos basados en múltiples carreras (para recomendaciones personalizadas).
     */
    @Transactional(readOnly = true)
    public LearningResourceResponse getResourcesByMultipleCareers(Long userId, List<Long> careerIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningResource> resourcePage = learningResourceRepository.findByCareerIdIn(careerIds, pageable);

        return buildResponse(resourcePage, userId, "Recursos sugeridos basados en tu perfil vocacional");
    }

    /**
     * Obtiene recursos sugeridos basados en múltiples áreas de interés.
     */
    @Transactional(readOnly = true)
    public LearningResourceResponse getResourcesByMultipleAreas(Long userId, List<Long> areaIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningResource> resourcePage = learningResourceRepository.findByAreaInteresIdIn(areaIds, pageable);

        return buildResponse(resourcePage, userId, "Recursos sugeridos basados en tus intereses");
    }

    /**
     * Obtiene todos los recursos guardados por el usuario.
     */
    @Transactional(readOnly = true)
    public LearningResourceResponse getSavedResources(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SavedResource> savedPage = savedResourceRepository.findByUserId(userId, pageable);

        List<Long> resourceIds = savedPage.getContent().stream()
                .map(sr -> sr.getId().getResourceId())
                .collect(Collectors.toList());

        List<LearningResource> resources = learningResourceRepository.findAllById(resourceIds);

        List<LearningResourceDto> dtos = resources.stream()
                .map(resource -> mapToDto(resource, true))
                .collect(Collectors.toList());

        return new LearningResourceResponse(
                dtos,
                savedPage.getNumber(),
                savedPage.getTotalPages(),
                savedPage.getTotalElements(),
                "Tus recursos guardados"
        );
    }

    /**
     * Guarda un recurso en favoritos.
     */
    @Transactional
    public void saveResource(Long userId, Long resourceId) {
        if (savedResourceRepository.existsByUserIdAndResourceId(userId, resourceId)) {
            return; // Ya está guardado
        }

        SavedResource savedResource = new SavedResource();
        savedResource.setId(new SavedResource.SavedResourceId(userId, resourceId));
        savedResourceRepository.save(savedResource);
    }

    /**
     * Guarda un recurso en favoritos con validación de enlace externo.
     */
    @Transactional
    public void saveResourceWithValidation(Long userId, Long resourceId, UrlValidationService urlValidationService) {
        if (savedResourceRepository.existsByUserIdAndResourceId(userId, resourceId)) {
            return; // Ya está guardado
        }

        // Obtener el recurso para validar si es enlace externo
        LearningResource resource = learningResourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Recurso no encontrado"));

        // Si es enlace externo, validar la URL
        if (resource.getTipoRecurso() != null && resource.getTipoRecurso().isEnlaceExterno()) {
            urlValidationService.validateExternalLink(resource.getUrlRecurso());
        }

        SavedResource savedResource = new SavedResource();
        savedResource.setId(new SavedResource.SavedResourceId(userId, resourceId));
        savedResourceRepository.save(savedResource);
    }

    /**
     * Elimina un recurso de favoritos.
     */
    @Transactional
    public void unsaveResource(Long userId, Long resourceId) {
        savedResourceRepository.deleteByIdUserIdAndIdResourceId(userId, resourceId);
    }

    /**
     * Verifica si un recurso está guardado.
     */
    @Transactional(readOnly = true)
    public boolean isResourceSaved(Long userId, Long resourceId) {
        return savedResourceRepository.existsByUserIdAndResourceId(userId, resourceId);
    }

    // Métodos privados auxiliares

    private LearningResourceResponse buildResponse(Page<LearningResource> resourcePage, Long userId, String message) {
        Set<Long> savedResourceIds = Set.copyOf(savedResourceRepository.findResourceIdsByUserId(userId));

        List<LearningResourceDto> dtos = resourcePage.getContent().stream()
                .map(resource -> mapToDto(resource, savedResourceIds.contains(resource.getId())))
                .collect(Collectors.toList());

        return new LearningResourceResponse(
                dtos,
                resourcePage.getNumber(),
                resourcePage.getTotalPages(),
                resourcePage.getTotalElements(),
                message
        );
    }

    private LearningResourceDto mapToDto(LearningResource resource, boolean isSaved) {
        return new LearningResourceDto(
                resource.getId(),
                resource.getCareerId(),
                resource.getTitulo(),
                resource.getUrlRecurso(),
                resource.getDescripcion(),
                resource.getDuracionMinutos(),
                resource.getAreaInteresId(),
                isSaved,
                resource.getTipoRecurso(),
                resource.getTipoRecurso() != null ? resource.getTipoRecurso().isDescargable() : false,
                resource.getTipoRecurso() != null ? resource.getTipoRecurso().isEnlaceExterno() : false,
                resource.getArchivoPdf(),
                resource.getUrlValida()
        );
    }
}

