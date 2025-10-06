package com.acme.vocatio.controller;

import com.acme.vocatio.dto.learningresource.LearningResourceResponse;
import com.acme.vocatio.dto.learningresource.SaveResourceRequest;
import com.acme.vocatio.security.UserPrincipal;
import com.acme.vocatio.service.LearningResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de recursos de aprendizaje.
 * Endpoints para ver materiales sugeridos por carrera, área vocacional y gestionar favoritos.
 */
@RestController
@RequestMapping("/api/learning-resources")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    /**
     * GET /api/learning-resources/by-career/{careerId}
     * Obtiene recursos de aprendizaje sugeridos para una carrera específica.
     */
    @GetMapping("/by-career/{careerId}")
    public ResponseEntity<LearningResourceResponse> getResourcesByCareer(
            @PathVariable Long careerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LearningResourceResponse response = learningResourceService.getResourcesByCareer(
                principal.getUser().getId(), careerId, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/learning-resources/by-area/{areaId}
     * Obtiene recursos de aprendizaje sugeridos para un área de interés específica.
     */
    @GetMapping("/by-area/{areaId}")
    public ResponseEntity<LearningResourceResponse> getResourcesByArea(
            @PathVariable Long areaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LearningResourceResponse response = learningResourceService.getResourcesByArea(
                principal.getUser().getId(), areaId, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/learning-resources/recommended
     * Obtiene recursos personalizados basados en carreras del perfil vocacional.
     */
    @GetMapping("/recommended")
    public ResponseEntity<LearningResourceResponse> getRecommendedResources(
            @RequestParam List<Long> careerIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LearningResourceResponse response = learningResourceService.getResourcesByMultipleCareers(
                principal.getUser().getId(), careerIds, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/learning-resources/by-interests
     * Obtiene recursos basados en múltiples áreas de interés del usuario.
     */
    @GetMapping("/by-interests")
    public ResponseEntity<LearningResourceResponse> getResourcesByInterests(
            @RequestParam List<Long> areaIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LearningResourceResponse response = learningResourceService.getResourcesByMultipleAreas(
                principal.getUser().getId(), areaIds, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/learning-resources/saved
     * Obtiene todos los recursos guardados como favoritos por el usuario.
     */
    @GetMapping("/saved")
    public ResponseEntity<LearningResourceResponse> getSavedResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LearningResourceResponse response = learningResourceService.getSavedResources(
                principal.getUser().getId(), page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/learning-resources/save
     * Guarda un recurso en favoritos.
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveResource(
            @Valid @RequestBody SaveResourceRequest request,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        learningResourceService.saveResource(principal.getUser().getId(), request.resourceId());

        return ResponseEntity.ok(Map.of("message", "Recurso guardado en favoritos"));
    }

    /**
     * DELETE /api/learning-resources/unsave/{resourceId}
     * Elimina un recurso de favoritos.
     */
    @DeleteMapping("/unsave/{resourceId}")
    public ResponseEntity<Map<String, String>> unsaveResource(
            @PathVariable Long resourceId,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        learningResourceService.unsaveResource(principal.getUser().getId(), resourceId);

        return ResponseEntity.ok(Map.of("message", "Recurso eliminado de favoritos"));
    }

    /**
     * GET /api/learning-resources/{resourceId}/is-saved
     * Verifica si un recurso está guardado en favoritos.
     */
    @GetMapping("/{resourceId}/is-saved")
    public ResponseEntity<Map<String, Boolean>> isResourceSaved(
            @PathVariable Long resourceId,
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean isSaved = learningResourceService.isResourceSaved(principal.getUser().getId(), resourceId);

        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }
}
