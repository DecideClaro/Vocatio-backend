package com.acme.vocatio.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponse {
    private String id;
    private String status;
    private ProgressInfo progress;
    private List<PageInfo> pages;
    private FeaturesInfo features;
    private List<SavedAnswer> answers;
    private MetadataInfo metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {
        private Integer currentPage;
        private Integer totalPages;
        private Integer answeredQuestions;
        private Integer totalQuestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private Integer page;
        private List<QuestionInfo> questions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionInfo {
        private String id;
        private String title;
        private Boolean required;
        private List<OptionInfo> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private String id;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturesInfo {
        private Boolean allowSaveForLater;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedAnswer {
        private String questionId;
        private String optionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataInfo {
        private Map<String, String> aria;
    }
}
