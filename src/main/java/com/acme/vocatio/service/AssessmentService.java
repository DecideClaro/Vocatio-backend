package com.acme.vocatio.service;

import com.acme.vocatio.dto.assessment.*;
import com.acme.vocatio.model.*;
import com.acme.vocatio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final TestVocacionalRepository testRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CareerRepository careerRepository;

    @Transactional
    public AssessmentResponse createAssessment(User user) {
        // Check if user already has an in-progress assessment
        Optional<Assessment> existing = assessmentRepository.findInProgressAssessmentByUser(user);
        if (existing.isPresent()) {
            throw new IllegalStateException("Ya tienes un test pendiente");
        }

        // Get the default test (ID = 1, or first available)
        TestVocacional test = testRepository.findById(1)
                .or(() -> testRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new RuntimeException("No hay tests disponibles"));

        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setTest(test);
        assessment.setStatus(Assessment.AssessmentStatus.IN_PROGRESS);

        assessment = assessmentRepository.save(assessment);

        return buildAssessmentResponse(assessment, test);
    }

    @Transactional
    public AssessmentResponse saveProgress(Long assessmentId, User user, SaveAnswersRequest request) {
        Assessment assessment = assessmentRepository.findByIdAndUser(assessmentId, user)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

// ==============================================================================
        // >> INICIO CAMBIO M2-01: Evidencia de Validación y Progreso
        // ==============================================================================

        log.info("[M2-01] Guardando progreso para Test ID: {} por Usuario ID: {}",
                assessmentId, user.getId());

        if (assessment.getStatus() != Assessment.AssessmentStatus.IN_PROGRESS) {
            log.warn("[M2-01] Intento de guardar progreso en un test que NO está 'EN_PROGRESO'. Estado: {}",
                    assessment.getStatus());
            throw new IllegalStateException("La evaluacion ya fue enviada o finalizada");
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            log.warn("[M2-01] No se recibieron respuestas para guardar.");
            // No lanzamos error, pero el log demuestra la validación de entrada
            // (Criterio: "intento avanzar sin responder")
        }
        // Validate and save answers
        Map<String, AssessmentAnswer> existingAnswers = assessment.getAnswers().stream()
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getId().toString(),
                        a -> a
                ));

        
        for (SaveAnswersRequest.AnswerInput input : request.getAnswers()) {
            try {
                Integer questionId = Integer.parseInt(input.getQuestionId());
                Integer optionId = Integer.parseInt(input.getOptionId());

                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
                
                Option option = optionRepository.findById(optionId)
                        .orElseThrow(() -> new RuntimeException("Opcion no encontrada"));

                // Update or create answer
                AssessmentAnswer answer = existingAnswers.get(input.getQuestionId());
                if (answer == null) {
                    answer = new AssessmentAnswer();
                    answer.setAssessment(assessment);
                    answer.setQuestion(question);
                    assessment.getAnswers().add(answer);
                }
                answer.setSelectedOption(option);
                answer.setAnsweredAt(Instant.now());

            } catch (NumberFormatException e) {
                throw new RuntimeException("ID de pregunta u opcion invalido");
            }
        }

        assessment = assessmentRepository.save(assessment);

        return buildAssessmentResponse(assessment, assessment.getTest());
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long assessmentId, User user) {
        Assessment assessment = assessmentRepository.findByIdWithAnswers(assessmentId)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            log.warn("[M2-03] Acceso denegado (403)al test ID: {} para Usuario ID: {}",
                    assessmentId, user.getId());
            throw new SecurityException("Acceso denegado");
        }

        return buildAssessmentResponse(assessment, assessment.getTest());
    }

    @Transactional
    public SubmitAssessmentResponse submitAssessment(Long assessmentId, User user) {
        Assessment assessment = assessmentRepository.findByIdWithAnswers(assessmentId)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Acceso denegado");
        }

        if (assessment.getStatus() == Assessment.AssessmentStatus.COMPLETED) {
            return SubmitAssessmentResponse.builder()
                    .assessmentId(assessmentId.toString())
                    .resultId(assessment.getResult() != null ? assessment.getResult().getId().toString() : null)
                    .status("COMPLETED")
                    .message("La evaluacion ya fue enviada")
                    .build();
        }

        // Calculate results
        Map<AreaInterest, Long> scores = assessment.getAnswers().stream()
                .map(a -> a.getSelectedOption().getAreaInteres())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

        AssessmentResult result = new AssessmentResult();
        result.setAssessment(assessment);

        for (Map.Entry<AreaInterest, Long> entry : scores.entrySet()) {
            AssessmentResultArea area = new AssessmentResultArea();
            area.setResult(result);
            area.setArea(entry.getKey());
            area.setScore(entry.getValue().intValue());
            result.getAreaScores().add(area);
        }

        result = assessmentResultRepository.save(result);
        
        assessment.setStatus(Assessment.AssessmentStatus.COMPLETED);
        assessment.setCompletedAt(Instant.now());
        assessment.setResult(result);
        assessmentRepository.save(assessment);

        return SubmitAssessmentResponse.builder()
                .assessmentId(assessmentId.toString())
                .resultId(result.getId().toString())
                .status("COMPLETED")
                .build();
    }

    @Transactional(readOnly = true)
    public AssessmentResultResponse getResult(Long assessmentId, User user) {
        Assessment assessment = assessmentRepository.findByIdWithResult(assessmentId)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Acceso denegado");
        }

        if (assessment.getResult() == null) {
            throw new RuntimeException("Resultado no disponible");
        }

        List<AssessmentResultResponse.AreaScore> topAreas = assessment.getResult().getAreaScores().stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .limit(3)
                .map(area -> AssessmentResultResponse.AreaScore.builder()
                        .code(area.getArea().getId().toString())
                        .name(area.getArea().getNombreArea())
                        .score(area.getScore())
                        .summary(area.getArea().getDescripcion())
                        .build())
                .collect(Collectors.toList());

        List<AssessmentResultResponse.CareerSuggestion> careers = careerRepository.findAll().stream()
                .limit(5)
                .map(career -> AssessmentResultResponse.CareerSuggestion.builder()
                        .id(career.getId())
                        .name(career.getNombre())
                        .build())
                .collect(Collectors.toList());

        List<Integer> chartSeries = assessment.getResult().getAreaScores().stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .map(AssessmentResultArea::getScore)
                .collect(Collectors.toList());

        return AssessmentResultResponse.builder()
                .assessmentId(assessmentId.toString())
                .topAreas(topAreas)
                .suggestedCareers(careers)
                .chart(AssessmentResultResponse.ChartData.builder()
                        .type("radar")
                        .series(chartSeries)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public AssessmentListResponse listAssessments(User user) {
        List<Assessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(user);

        List<AssessmentListResponse.AssessmentItem> items = assessments.stream()
                .map(assessment -> {
                    List<AssessmentListResponse.TopAreaInfo> topAreas = new ArrayList<>();
                    
                    if (assessment.getResult() != null) {
                        topAreas = assessment.getResult().getAreaScores().stream()
                                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                                .limit(2)
                                .map(area -> AssessmentListResponse.TopAreaInfo.builder()
                                        .code(area.getArea().getId().toString())
                                        .score(area.getScore())
                                        .build())
                                .collect(Collectors.toList());
                    }

                    return AssessmentListResponse.AssessmentItem.builder()
                            .id(assessment.getId().toString())
                            .status(assessment.getStatus().toString())
                            .completedAt(assessment.getCompletedAt() != null ? 
                                    assessment.getCompletedAt().toString() : null)
                            .topAreas(topAreas)
                            .links(AssessmentListResponse.LinksInfo.builder()
                                    .detail("/assessments/" + assessment.getId())
                                    .report("/assessments/" + assessment.getId() + "/report.pdf")
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());

        return AssessmentListResponse.builder()
                .items(items)
                .build();
    }

    @Transactional
    public void deleteAssessment(Long assessmentId, User user) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            log.error("[M2-04] Intento de borrado negado (403) Usuario {} intentó borrar Test ID {}",
                    user.getId(), assessmentId);
            throw new SecurityException("Acceso denegado");
        }

        assessmentRepository.delete(assessment);
    }

    private AssessmentResponse buildAssessmentResponse(Assessment assessment, TestVocacional test) {
        // Build pages with questions
        List<AssessmentResponse.PageInfo> pages = new ArrayList<>();
        
        if (test.getQuestions() != null && !test.getQuestions().isEmpty()) {
            List<Question> allQuestions = new ArrayList<>(test.getQuestions());
            int pageSize = 10;
            int totalPages = (int) Math.ceil((double) allQuestions.size() / pageSize);

            for (int i = 0; i < totalPages; i++) {
                int start = i * pageSize;
                int end = Math.min(start + pageSize, allQuestions.size());
                List<Question> pageQuestions = allQuestions.subList(start, end);

                List<AssessmentResponse.QuestionInfo> questionInfos = pageQuestions.stream()
                        .map(q -> AssessmentResponse.QuestionInfo.builder()
                                .id(q.getId().toString())
                                .title(q.getTextoPregunta())
                                .required(true)
                                .options(q.getOpciones() != null ? q.getOpciones().stream()
                                        .map(opt -> AssessmentResponse.OptionInfo.builder()
                                                .id(opt.getId().toString())
                                                .label(opt.getTextoOpcion())
                                                .build())
                                        .collect(Collectors.toList()) : new ArrayList<>())
                                .build())
                        .collect(Collectors.toList());

                pages.add(AssessmentResponse.PageInfo.builder()
                        .page(i + 1)
                        .questions(questionInfos)
                        .build());
            }
        }

        // Build saved answers
        List<AssessmentResponse.SavedAnswer> savedAnswers = assessment.getAnswers().stream()
                .map(a -> AssessmentResponse.SavedAnswer.builder()
                        .questionId(a.getQuestion().getId().toString())
                        .optionId(a.getSelectedOption().getId().toString())
                        .build())
                .collect(Collectors.toList());

        int totalQuestions = test.getQuestions() != null ? test.getQuestions().size() : 0;
        
        return AssessmentResponse.builder()
                .id(assessment.getId().toString())
                .status(assessment.getStatus().toString())
                .progress(AssessmentResponse.ProgressInfo.builder()
                        .currentPage(1)
                        .totalPages(pages.size())
                        .answeredQuestions(assessment.getAnswers().size())
                        .totalQuestions(totalQuestions)
                        .build())
                .pages(pages)
                .features(AssessmentResponse.FeaturesInfo.builder()
                        .allowSaveForLater(true)
                        .build())
                .answers(savedAnswers)
                .metadata(AssessmentResponse.MetadataInfo.builder()
                        .aria(Map.of("nextButton", "Siguiente pagina"))
                        .build())
                .build();
    }
}
