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
public class AssessmentListResponse {
    private List<AssessmentItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentItem {
        private String id;
        private String status;
        private String completedAt;
        private List<TopAreaInfo> topAreas;
        private LinksInfo links;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAreaInfo {
        private String code;
        private Integer score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinksInfo {
        private String detail;
        private String report;
    }
}
