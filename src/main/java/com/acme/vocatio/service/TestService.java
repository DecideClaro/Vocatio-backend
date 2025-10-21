package com.acme.vocatio.service;

import com.acme.vocatio.dto.answers.AnswerAreaDTO;
import com.acme.vocatio.dto.answers.AnswerDTO;
import com.acme.vocatio.model.TestVocacional;
import java.util.List;

public interface TestService {
    TestVocacional getTestById(Integer idTest);
    List<AnswerAreaDTO> submitTest(List<AnswerDTO> respuestas);
}
