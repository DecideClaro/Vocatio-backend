package com.acme.vocatio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.exception.CareerNotFoundException;
import com.acme.vocatio.model.Career;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.CareerRepository;
import com.acme.vocatio.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class FavoriteCareerServiceTest {

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private FavoriteCareerService favoriteCareerService;

    @BeforeEach
    void setUp() {
        favoriteCareerService = new FavoriteCareerService(userRepository, careerRepository);
        entityManager.getEntityManager().createNativeQuery("""
                create table if not exists profiles (
                    id_usuario bigint primary key,
                    age smallint,
                    grade varchar(64),
                    name varchar(255),
                    personal_interests varchar(255),
                    public_preferences varchar(255)
                )
                """)
                .executeUpdate();
    }

    @Test
    void givenCareer_whenAddFavorite_thenItIsPersistedAndReturned() {
        User user = persistUser("ana@example.com");
        Career career = persistCareer("Ingeniería Industrial", "4 años");

        CareerListDto dto = favoriteCareerService.addFavorite(user.getId(), career.getId());
        entityManager.flush();
        entityManager.clear();

        User refreshed = userRepository.findWithFavoriteCareersById(user.getId()).orElseThrow();
        assertThat(refreshed.getFavoriteCareers()).hasSize(1);
        assertThat(refreshed.getFavoriteCareers()).first().extracting(Career::getNombre)
                .isEqualTo("Ingeniería Industrial");

        assertThat(dto.id()).isEqualTo(career.getId());
        assertThat(dto.nombre()).isEqualTo("Ingeniería Industrial");
        assertThat(dto.duracion()).isEqualTo("4 años");
    }

    @Test
    void givenFavorite_whenRemove_thenItIsDeleted() {
        User user = persistUser("luis@example.com");
        Career career = persistCareer("Diseño Gráfico", "3 años");
        favoriteCareerService.addFavorite(user.getId(), career.getId());
        entityManager.flush();
        entityManager.clear();

        favoriteCareerService.removeFavorite(user.getId(), career.getId());
        entityManager.flush();
        entityManager.clear();

        User refreshed = userRepository.findWithFavoriteCareersById(user.getId()).orElseThrow();
        assertThat(refreshed.getFavoriteCareers()).isEmpty();
    }

    @Test
    void givenMultipleFavorites_whenList_thenReturnsAlphabetically() {
        User user = persistUser("sofia@example.com");
        Career bio = persistCareer("Biología", "5 años");
        Career admin = persistCareer("Administración", "4 años");
        Career zootecnia = persistCareer("Zootecnia", "5 años");

        favoriteCareerService.addFavorite(user.getId(), bio.getId());
        favoriteCareerService.addFavorite(user.getId(), admin.getId());
        favoriteCareerService.addFavorite(user.getId(), zootecnia.getId());

        List<CareerListDto> favorites = favoriteCareerService.listFavorites(user.getId());

        assertThat(favorites).hasSize(3);
        assertThat(favorites).extracting(CareerListDto::nombre)
                .containsExactly("Administración", "Biología", "Zootecnia");
    }

    @Test
    void givenUnknownCareer_whenAddFavorite_thenThrowsNotFound() {
        User user = persistUser("carlos@example.com");

        assertThatThrownBy(() -> favoriteCareerService.addFavorite(user.getId(), 999L))
                .isInstanceOf(CareerNotFoundException.class)
                .hasMessage("Carrera no encontrada");
    }

    private User persistUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("secret");
        return entityManager.persistAndFlush(user);
    }

    private Career persistCareer(String nombre, String duracion) {
        Career career = new Career();
        career.setNombre(nombre);
        career.setDuracion(duracion);
        career.setDescripcion("Descripción de " + nombre);
        return entityManager.persistAndFlush(career);
    }
}
