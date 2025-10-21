package com.acme.vocatio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.vocatio.dto.career.CareerDetailDto;
import com.acme.vocatio.dto.career.CareerListDto;
import com.acme.vocatio.dto.career.CareerPageResponse;
import com.acme.vocatio.exception.CareerNotFoundException;
import com.acme.vocatio.model.Career;
import com.acme.vocatio.model.Institution;
import com.acme.vocatio.repository.CareerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class CareerServiceTest {

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private CareerService careerService;

    @BeforeEach
    void setUp() {
        careerService = new CareerService(careerRepository);
    }

    @Test
    void givenNoCareers_whenList_thenReturnsEmptyMessage() {
        CareerPageResponse response = careerService.listCareers(PageRequest.of(0, 10), null, null, null);

        assertThat(response.careers()).isEmpty();
        assertThat(response.message()).isEqualTo("No hay carreras disponibles en este momento.");
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    void givenUnsortedPageRequest_whenList_thenAppliesDefaultSortingAndLimit() {
        persistCareer("Zoología", "5 años", "Presencial", "Ciencias", "Universitaria");
        persistCareer("Administración", "3 años", "Virtual", "Negocios", "Universitaria");
        persistCareer("Biología", "4 años", "Híbrida", "Ciencias", "Universitaria");

        CareerPageResponse response = careerService.listCareers(null, null, null, null);

        assertThat(response.size()).isEqualTo(20);
        assertThat(response.careers())
                .extracting(CareerListDto::nombre)
                .containsExactly("Administración", "Biología", "Zoología");
        assertThat(response.message()).isNull();
    }

    @Test
    void givenFilters_whenList_thenReturnsMatchingCareers() {
        persistCareer("Tec. Agro", "2 años", "Presencial", "Agro", "Técnica");
        persistCareer("Ing. Sistemas", "5 años", "Virtual", "Tecnologia", "Universitaria");
        persistCareer("Ing. Civil", "5 años", "Presencial", "Tecnologia", "Universitaria");

        CareerPageResponse response = careerService.listCareers(
                PageRequest.of(0, 5),
                "  tecnologia  ",
                "5",
                "universitaria"
        );

        assertThat(response.careers()).hasSize(2);
        assertThat(response.careers())
                .extracting(CareerListDto::nombre)
                .containsExactly("Ing. Civil", "Ing. Sistemas");
        assertThat(response.message()).isNull();
    }

    private void persistCareer(String nombre, String duracion, String modalidad, String area, String tipo) {
        Career career = new Career();
        career.setNombre(nombre);
        career.setDescripcion("Descripción de " + nombre);
        career.setDuracion(duracion);
        career.setModalidad(modalidad);
        career.setAreaInteres(area);
        career.setTipoFormacion(tipo);
        entityManager.persistAndFlush(career);
    }

    @Test
    void givenExistingCareer_whenGetDetail_thenReturnsExpandedInformation() {
        Institution uni = new Institution();
        uni.setNombre("Universidad Central");
        entityManager.persist(uni);

        Institution inst = new Institution();
        inst.setNombre("Instituto Técnico del Sur");
        entityManager.persist(inst);

        Career career = new Career();
        career.setNombre("Ingeniería de Datos");
        career.setDuracion("5 años");
        career.setModalidad("Híbrida");
        career.setPlanEstudiosBasico("Bases de programación, matemáticas y arquitectura de datos.");
        career.setPerfilEgreso("Profesionales capaces de diseñar pipelines de datos escalables.");
        career.getInstituciones().add(uni);
        career.getInstituciones().add(inst);
        entityManager.persistAndFlush(career);

        CareerDetailDto detail = careerService.getCareerDetail(career.getId());

        assertThat(detail.nombre()).isEqualTo("Ingeniería de Datos");
        assertThat(detail.duracion()).isEqualTo("5 años");
        assertThat(detail.modalidad()).isEqualTo("Híbrida");
        assertThat(detail.planEstudiosBasico()).contains("programación");
        assertThat(detail.perfilEgreso()).contains("pipelines de datos");
        assertThat(detail.instituciones()).containsExactly("Instituto Técnico del Sur", "Universidad Central");
    }

    @Test
    void givenMissingCareer_whenGetDetail_thenThrowsNotFound() {
        assertThatThrownBy(() -> careerService.getCareerDetail(999L))
                .isInstanceOf(CareerNotFoundException.class)
                .hasMessage("Carrera no encontrada");
    }
}
