package com.acme.vocatio.dto.assessment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAssessmentResponse {
    private String assessmentId;
    private String resultId;
    private String status;
    private String message;
}
