package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessment_results")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentResultArea> areaScores = new ArrayList<>();

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @PrePersist
    void onCreate() {
        this.calculatedAt = Instant.now();
    }
}
