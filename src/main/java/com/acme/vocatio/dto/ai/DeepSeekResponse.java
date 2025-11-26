package com.acme.vocatio.dto.ai;

import java.util.List;

public record DeepSeekResponse(
        String mbtiProfile, List<String> suggestedCareers, List<String> qualities, String profileSummary) {}
