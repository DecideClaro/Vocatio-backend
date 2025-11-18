package com.acme.vocatio.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/** Entidad JPA del perfil individual. */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode personalInterests;

    @Column(name = "public_preferences", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode publicPreferences;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void setPersonalInterests(String json) {

        if (json == null || json.isBlank()) {
            this.personalInterests = null;
            return;
        }
        try {
            this.personalInterests = OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON: " + json, e);
        }
    }

    public void setPublicPreferences(String json) {
        if (json == null || json.isBlank()) {
            this.publicPreferences = null;
            return;
        }
        try {
            this.publicPreferences = OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON: " + json, e);
        }
    }

    public JsonNode getPublicPreferences() {
        return publicPreferences != null ? publicPreferences : OBJECT_MAPPER.createObjectNode();
    }

    public JsonNode getPersonalInterests() {
        return personalInterests != null ? personalInterests : OBJECT_MAPPER.createObjectNode();
    }
}
