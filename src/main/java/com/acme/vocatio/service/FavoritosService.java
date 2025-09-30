package com.acme.vocatio.service;

import com.acme.vocatio.model.Carrera;
import com.acme.vocatio.model.UsuarioFavoritos;

import com.acme.vocatio.repository.UsuarioFavoritosRepository;
import com.acme.vocatio.repository.CarreraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritosService {

    private final UsuarioFavoritosRepository usuarioFavoritosRepository;
    private final CarreraRepository carreraRepository;

    @Transactional
    public boolean agregarFavorito(Long idUsuario, Long idCarrera) {
        // Verificar que la carrera existe
        if (!carreraRepository.existsById(idCarrera)) {
            return false;
        }

        // Verificar que no esté ya en favoritos
        if (usuarioFavoritosRepository.existsByIdUsuarioAndIdCarrera(idUsuario, idCarrera)) {
            return false;
        }

        UsuarioFavoritos favorito = new UsuarioFavoritos();
        favorito.setIdUsuario(idUsuario);
        favorito.setIdCarrera(idCarrera);
        
        usuarioFavoritosRepository.save(favorito);
        return true;
    }

    @Transactional
    public boolean eliminarFavorito(Long idUsuario, Long idCarrera) {
        if (usuarioFavoritosRepository.existsByIdUsuarioAndIdCarrera(idUsuario, idCarrera)) {
            usuarioFavoritosRepository.deleteByIdUsuarioAndIdCarrera(idUsuario, idCarrera);
            return true;
        }
        return false;
    }

    public List<Carrera> obtenerFavoritosDeUsuario(Long idUsuario) {
        List<UsuarioFavoritos> favoritos = usuarioFavoritosRepository.findFavoritosByUsuarioConCarrera(idUsuario);
        return favoritos.stream()
                .map(UsuarioFavoritos::getCarrera)
                .collect(Collectors.toList());
    }

    public boolean esCarreraFavorita(Long idUsuario, Long idCarrera) {
        return usuarioFavoritosRepository.existsByIdUsuarioAndIdCarrera(idUsuario, idCarrera);
    }

    public int contarFavoritosDeUsuario(Long idUsuario) {
        return usuarioFavoritosRepository.findByIdUsuario(idUsuario).size();
    }
}