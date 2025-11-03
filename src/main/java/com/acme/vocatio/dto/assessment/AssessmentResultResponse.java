package com.acme.vocatio.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultResponse {
    private String assessmentId;
    private List<AreaScore> topAreas;
    private List<CareerSuggestion> suggestedCareers;
    private ChartData chart;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaScore {
        private String code;
        private String name;
        private Integer score;
        private String summary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CareerSuggestion {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String type;
        private List<Integer> series;
    }
}
