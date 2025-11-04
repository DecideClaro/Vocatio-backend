package com.acme.vocatio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assessment_result_areas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"result_id", "area_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AssessmentResultArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private AssessmentResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private AreaInterest area;

    @Column(nullable = false)
    private Integer score;
}
