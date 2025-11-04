package com.acme.vocatio.repository;

import com.acme.vocatio.model.Assessment;
import com.acme.vocatio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    
    List<Assessment> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.answers WHERE a.id = :id")
    Optional<Assessment> findByIdWithAnswers(@Param("id") Long id);
    
    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.result r LEFT JOIN FETCH r.areaScores WHERE a.id = :id")
    Optional<Assessment> findByIdWithResult(@Param("id") Long id);
    
    Optional<Assessment> findByIdAndUser(Long id, User user);
    
    @Query("SELECT a FROM Assessment a WHERE a.user = :user AND a.status = 'IN_PROGRESS'")
    Optional<Assessment> findInProgressAssessmentByUser(@Param("user") User user);
}
