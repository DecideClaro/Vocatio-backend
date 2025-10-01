package com.acme.vocatio.controller;

import com.acme.vocatio.model.Carrera;
import com.acme.vocatio.service.FavoritosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class FavoritosController {

    private final FavoritosService favoritosService;

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, Object>> agregarFavorito(
            @RequestParam Long idCarrera,
            Authentication authentication) {
        
        // Obtener el ID del usuario autenticado (asumiendo que el email está en authentication.getName())
        // Esto necesitará adaptarse según tu sistema de autenticación actual
        Long idUsuario = obtenerIdUsuarioDeAuthentication(authentication);
        
        boolean agregado = favoritosService.agregarFavorito(idUsuario, idCarrera);
        
        if (agregado) {
            return ResponseEntity.ok(Map.of(
                "mensaje", "Carrera agregada a favoritos",
                "exito", true
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "mensaje", "No se pudo agregar la carrera a favoritos",
                "exito", false
            ));
        }
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<Map<String, Object>> eliminarFavorito(
            @RequestParam Long idCarrera,
            Authentication authentication) {
        
        Long idUsuario = obtenerIdUsuarioDeAuthentication(authentication);
        
        boolean eliminado = favoritosService.eliminarFavorito(idUsuario, idCarrera);
        
        if (eliminado) {
            return ResponseEntity.ok(Map.of(
                "mensaje", "Carrera eliminada de favoritos",
                "exito", true
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "mensaje", "No se pudo eliminar la carrera de favoritos",
                "exito", false
            ));
        }
    }

    @GetMapping("/mis-favoritos")
    public ResponseEntity<List<Carrera>> obtenerMisFavoritos(Authentication authentication) {
        Long idUsuario = obtenerIdUsuarioDeAuthentication(authentication);
        List<Carrera> favoritos = favoritosService.obtenerFavoritosDeUsuario(idUsuario);
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/es-favorita/{idCarrera}")
    public ResponseEntity<Map<String, Boolean>> esCarreraFavorita(
            @PathVariable Long idCarrera,
            Authentication authentication) {
        
        Long idUsuario = obtenerIdUsuarioDeAuthentication(authentication);
        boolean esFavorita = favoritosService.esCarreraFavorita(idUsuario, idCarrera);
        
        return ResponseEntity.ok(Map.of("esFavorita", esFavorita));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Integer>> obtenerEstadisticas(Authentication authentication) {
        Long idUsuario = obtenerIdUsuarioDeAuthentication(authentication);
        int totalFavoritos = favoritosService.contarFavoritosDeUsuario(idUsuario);
        
        return ResponseEntity.ok(Map.of("totalFavoritos", totalFavoritos));
    }

    /**
     * Helper method para obtener el ID del usuario desde la autenticación.
     * Esto necesitará ser adaptado según tu implementación actual de autenticación.
     */
    private Long obtenerIdUsuarioDeAuthentication(Authentication authentication) {
        // TODO: Implementar según tu sistema de autenticación actual
        // Por ejemplo, si guardas el ID en el token JWT o en UserDetails
        // Por ahora, retornamos 1L como placeholder
        return 1L;
    }
}