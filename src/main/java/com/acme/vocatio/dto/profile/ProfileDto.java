package com.acme.vocatio.dto.profile;

import java.util.List;
import java.util.Map;

/** Vista consolidada del perfil personal. */
public record ProfileDto(
        Long id,
        String email,
        String name,
        Integer age,
        String grade,
        String gradeLabel,
        List<String> interests,
        Map<String, Boolean> preferences) {}
