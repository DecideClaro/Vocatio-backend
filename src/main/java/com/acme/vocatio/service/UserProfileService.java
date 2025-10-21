package com.acme.vocatio.service;

import com.acme.vocatio.dto.profile.PersonalDataUpdateRequest;
import com.acme.vocatio.dto.profile.ProfileDto;
import com.acme.vocatio.dto.profile.ProfileUpdateRequest;
import com.acme.vocatio.exception.InvalidPersonalDataException;
import com.acme.vocatio.exception.UserNotFoundException;
import com.acme.vocatio.model.AcademicGrade;
import com.acme.vocatio.model.Profile;
import com.acme.vocatio.model.User;
import com.acme.vocatio.repository.ProfileRepository;
import com.acme.vocatio.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquesta la gestión del perfil actual. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Boolean>> STRING_BOOLEAN_MAP_TYPE = new TypeReference<>() {};

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    /** Devuelve el perfil del usuario autenticado. */
    public ProfileDto getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Profile profile = profileRepository.findById(userId).orElse(null);
        return toDto(user, profile);
    }

    /** Actualiza nombre y preferencias públicas. */
    @Transactional
    public ProfileDto updatePersonalData(Long userId, PersonalDataUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profileRepository
                .findById(userId)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setUser(user);
        user.setProfile(profile);

        String normalizedName = request.name().trim();
        profile.setName(normalizedName);

        if (request.preferences() != null) {
            Map<String, Boolean> normalizedPreferences = normalizePreferences(request.preferences());
            profile.setPublicPreferences(writePreferences(normalizedPreferences));
        }

        Profile saved = profileRepository.save(profile);
        return toDto(user, saved);
    }

    /** Guarda los cambios de edad, grado e intereses. */
    @Transactional
    public ProfileDto updateCurrentUserProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Profile profile = profileRepository
                .findById(userId)
                .orElseGet(() -> {
                    Profile newProfile = new Profile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setUser(user);
        user.setProfile(profile);
        profile.setAge(request.age().shortValue());

        AcademicGrade grade = AcademicGrade
                .fromCode(request.grade())
                .orElseThrow(() -> new IllegalStateException("Grado académico no reconocido"));
        profile.setGrade(grade);

        List<String> normalizedInterests = normalizeInterests(request.interests());
        profile.setPersonalInterests(writeInterests(normalizedInterests));

        Profile saved = profileRepository.save(profile);
        return toDto(user, saved, normalizedInterests);
    }

    /** Limpia espacios y evita duplicados en intereses (insensible a mayúsculas). */
    private List<String> normalizeInterests(List<String> rawInterests) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (String interest : rawInterests) {
            if (interest == null) {
                continue;
            }

            String trimmed = interest.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String key = trimmed.toLowerCase(Locale.ROOT);
            normalized.putIfAbsent(key, trimmed);
        }
        return List.copyOf(normalized.values());
    }

    /** Serializa los intereses a JSON. */
    private String writeInterests(List<String> interests) {
        try {
            return objectMapper.writeValueAsString(interests);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible guardar los intereses del perfil", ex);
        }
    }

    /** Deserializa los intereses almacenados. */
    private List<String> readInterests(String interestsJson) {
        if (interestsJson == null || interestsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(interestsJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible leer los intereses almacenados", ex);
        }
    }

    /** Crea el DTO usando los datos existentes. */
    private ProfileDto toDto(User user, Profile profile) {
        List<String> interests = profile == null ? List.of() : readInterests(profile.getPersonalInterests());
        Map<String, Boolean> preferences =
                profile == null ? Map.of() : readPreferences(profile.getPublicPreferences());
        return toDto(user, profile, interests, preferences);
    }

    /** Ajusta el DTO con edad, grado e intereses. */
    private ProfileDto toDto(
            User user, Profile profile, List<String> interests, Map<String, Boolean> preferences) {
        Integer age = null;
        String gradeCode = null;
        String gradeLabel = null;
        String name = null;

        if (profile != null) {
            name = profile.getName();
            if (profile.getAge() != null) {
                age = profile.getAge().intValue();
            }
            if (profile.getGrade() != null) {
                gradeCode = profile.getGrade().getCode();
                gradeLabel = profile.getGrade().getDisplayName();
            }
        }

        List<String> safeInterests = interests == null ? List.of() : List.copyOf(interests);
        Map<String, Boolean> safePreferences = preferences == null ? Map.of() : Map.copyOf(preferences);

        return new ProfileDto(user.getId(), user.getEmail(), name, age, gradeCode, gradeLabel, safeInterests, safePreferences);
    }

    private ProfileDto toDto(User user, Profile profile, List<String> interests) {
        Map<String, Boolean> preferences =
                profile == null ? Map.of() : readPreferences(profile.getPublicPreferences());
        return toDto(user, profile, interests, preferences);
    }

    private Map<String, Boolean> normalizePreferences(Map<String, Boolean> rawPreferences) {
        Map<String, Boolean> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : rawPreferences.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }

            String trimmedKey = key.trim();
            if (trimmedKey.isEmpty()) {
                continue;
            }

            Boolean value = entry.getValue();
            if (value == null) {
                throw new InvalidPersonalDataException(
                        "El valor de la preferencia '" + trimmedKey + "' es obligatorio");
            }

            normalized.put(trimmedKey, value);
        }
        return Map.copyOf(normalized);
    }

    private String writePreferences(Map<String, Boolean> preferences) {
        try {
            return objectMapper.writeValueAsString(preferences);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible guardar las preferencias públicas", ex);
        }
    }

    private Map<String, Boolean> readPreferences(String preferencesJson) {
        if (preferencesJson == null || preferencesJson.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Boolean> preferences = objectMapper.readValue(preferencesJson, STRING_BOOLEAN_MAP_TYPE);
            Map<String, Boolean> sanitized = preferences.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
            return Map.copyOf(sanitized);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible leer las preferencias almacenadas", ex);
        }
    }
}
