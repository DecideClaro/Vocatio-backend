package com.acme.vocatio.service;

import com.acme.vocatio.model.Assessment;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.AssessmentRepository;
import com.acme.vocatio.repository.TestVocacionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

//import static jdk.internal.org.jline.reader.impl.LineReaderImpl.CompletionType.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private TestVocacionalRepository testRepository; // Mock del repositorio

    @Mock
    private AssessmentRepository assessmentRepository;

    @InjectMocks
    private AssessmentService assessmentService; // La clase que probamos

    @Test
    void whenNoTestsAvailable_createAssessment_thenThrowsException() {
        // Arrange (Organizar)
        // Simulamos que la BD está vacía (no encuentra ID=1 ni ningún otro)
        when(testRepository.findById(1)).thenReturn(Optional.empty());
        //when(testRepository.findAll()).thenReturn(List.notify()); // Simula que no hay tests disponibles
        User user = new User(); // Un usuario de prueba

        // Act & Assert (Actuar y Afirmar)
        // Verificamos que se lance la excepción exacta que vimos en el error 500
        assertThatThrownBy(() -> assessmentService.createAssessment(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No hay tests disponibles");
    }

    @Test
    void whenUserRequestsAnotherUsersAssessment_thenThrowsSecurityException() {
        // Arrange (Organizar)
        User ownerUser = new User(); // El dueño
        ownerUser.setId(1L);

        User attackerUser = new User(); // El atacante
        attackerUser.setId(99L);

        Assessment assessment = new Assessment();
        assessment.setUser(ownerUser); // El test pertenece al Dueño

        // Simulamos que el repositorio encuentra el test
        when(assessmentRepository.findByIdWithAnswers(123L)).thenReturn(Optional.of(assessment));

        // Act & Assert (Actuar y Afirmar)
        // Verificamos que se lance SecurityException al intentar leerlo
        assertThatThrownBy(() -> assessmentService.getAssessment(123L, attackerUser))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Acceso denegado");
    }
}

