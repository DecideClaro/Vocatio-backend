package com.acme.vocatio.repository;

import com.acme.vocatio.model.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
}
