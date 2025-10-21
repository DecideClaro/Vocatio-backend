package com.acme.vocatio.service;

import com.acme.vocatio.dto.answers.AnswerAreaDTO;
import com.acme.vocatio.dto.answers.AnswerDTO;
import com.acme.vocatio.model.AreaInterest;
import com.acme.vocatio.model.Option;
import com.acme.vocatio.model.TestVocacional;
import com.acme.vocatio.repository.OptionRepository;
import com.acme.vocatio.repository.TestVocacionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServicempl implements TestService {

    private final TestVocacionalRepository testVocacionalRepository;
    private final OptionRepository optionRepository;

    @Override
    public TestVocacional getTestById(Integer idTest) {
        return testVocacionalRepository.findByIdWithDetails(idTest)
                .orElseThrow(() -> new RuntimeException("Test no encontrado con ID: " + idTest));
    }

    @Override
    public List<AnswerAreaDTO> submitTest(List<AnswerDTO> respuestas) {
        // Obtenemos los IDs de las opciones seleccionadas
        List<Integer> idsOpciones = respuestas.stream()
                .map(AnswerDTO::getIdOpcionSeleccionada)
                .collect(Collectors.toList());

        // Buscamos todas las opciones seleccionadas en la base de datos de una sola vez
        List<Option> opcionesSeleccionadas = optionRepository.findAllById(idsOpciones);

        // Agrupamos por Área de Interés y contamos las ocurrencias (¡aquí está la magia!)
        Map<AreaInterest, Long> puntajesPorArea = opcionesSeleccionadas.stream()
                .map(Option::getAreaInteres)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Convertimos el mapa de resultados a una lista de DTOs y la ordenamos de mayor a menor puntaje
        return puntajesPorArea.entrySet().stream()
                .map(entry -> new AnswerAreaDTO(
                        entry.getKey().getNombreArea(),
                        entry.getValue().intValue(),
                        entry.getKey().getDescripcion()))
                .sorted(Comparator.comparingInt(AnswerAreaDTO::getPuntaje).reversed())
                .collect(Collectors.toList());
    }
}
