package com.acme.vocatio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad JPA del perfil individual. */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @Column(name = "id_usuario")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    private User user;

    private String name;

    private Short age;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private AcademicGrade grade;

    @Column(name = "personal_interests", columnDefinition = "jsonb")
    private String personalInterests;

    @Column(name = "public_preferences", columnDefinition = "jsonb")
    private String publicPreferences;
}
