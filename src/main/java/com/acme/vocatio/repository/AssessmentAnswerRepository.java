package com.acme.vocatio.repository;

import com.acme.vocatio.model.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {
}
