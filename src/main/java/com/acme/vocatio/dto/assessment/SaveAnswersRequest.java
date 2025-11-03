package com.acme.vocatio.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAnswersRequest {
    private List<AnswerInput> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerInput {
        private String questionId;
        private String optionId;
    }
}
