package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "assessment_answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assessment_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option selectedOption;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    @PrePersist
    void onCreate() {
        this.answeredAt = Instant.now();
    }
}
