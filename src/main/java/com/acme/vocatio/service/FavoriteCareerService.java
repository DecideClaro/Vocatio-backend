package com.acme.vocatio.service;

import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.exception.CareerNotFoundException;
import com.acme.vocatio.exception.UserNotFoundException;
import com.acme.vocatio.model.Career;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.CareerRepository;
import com.acme.vocatio.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteCareerService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;

    @Transactional
    public CareerListDto addFavorite(Long userId, Long careerId) {
        User user = getUserWithFavorites(userId);
        Career career = careerRepository.findById(careerId).orElseThrow(() -> new CareerNotFoundException(careerId));
        user.getFavoriteCareers().add(career);
        return toListDto(career);
    }

    @Transactional
    public void removeFavorite(Long userId, Long careerId) {
        if (!careerRepository.existsById(careerId)) {
            throw new CareerNotFoundException(careerId);
        }
        User user = getUserWithFavorites(userId);
        user.getFavoriteCareers().removeIf(career -> careerId.equals(career.getId()));
    }

    @Transactional(readOnly = true)
    public List<CareerListDto> listFavorites(Long userId) {
        User user = getUserWithFavorites(userId);
        return user.getFavoriteCareers()
                .stream()
                .sorted(Comparator.comparing(Career::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::toListDto)
                .toList();
    }

    private User getUserWithFavorites(Long userId) {
        return userRepository.findWithFavoriteCareersById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private CareerListDto toListDto(Career career) {
        return new CareerListDto(
                career.getId(),
                career.getNombre(),
                career.getDescripcion(),
                career.getDuracion(),
                career.getModalidad(),
                career.getAreaInteres(),
                career.getTipoFormacion());
    }
}
