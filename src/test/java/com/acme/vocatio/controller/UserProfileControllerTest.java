package com.acme.vocatio.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Pruebas del flujo GET/PUT del perfil. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnEmptyProfileWhenNoData() throws Exception {
        String email = "empty-profile@vocatio.com";
        String token = obtainAccessToken(email);

        mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.age").value(nullValue()))
                .andExpect(jsonPath("$.grade").value(nullValue()))
                .andExpect(jsonPath("$.gradeLabel").value(nullValue()))
                .andExpect(jsonPath("$.interests", hasSize(0)));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        String email = "update-profile@vocatio.com";
        String token = obtainAccessToken(email);

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "age": 18,
                                  "grade": "SUPERIOR_TECNICA_2",
                                  "interests": [
                                    "Tecnología",
                                    "Arte"
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Perfil actualizado"))
                .andExpect(jsonPath("$.profile.email").value(email))
                .andExpect(jsonPath("$.profile.age").value(18))
                .andExpect(jsonPath("$.profile.grade").value("SUPERIOR_TECNICA_2"))
                .andExpect(jsonPath("$.profile.gradeLabel").value("2° ciclo de instituto técnico"))
                .andExpect(jsonPath("$.profile.interests", contains("Tecnología", "Arte")));

        mockMvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.age").value(18))
                .andExpect(jsonPath("$.grade").value("SUPERIOR_TECNICA_2"))
                .andExpect(jsonPath("$.interests", contains("Tecnología", "Arte")));
    }

    @Test
    void shouldRejectAgeOutsideRange() throws Exception {
        String token = obtainAccessToken("invalid-age@vocatio.com");

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "age": 12,
                                  "grade": "SECUNDARIA_3",
                                  "interests": ["Tecnología"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age[0]").value("La edad debe estar entre 13 y 30 años"));
    }

    @Test
    void shouldRejectInvalidGrade() throws Exception {
        String token = obtainAccessToken("invalid-grade@vocatio.com");

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "age": 18,
                                  "grade": "UNIVERSIDAD_10",
                                  "interests": ["Tecnología"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.grade[0]").value("El grado académico no es válido"));
    }

    @Test
    void shouldRejectEmptyInterests() throws Exception {
        String token = obtainAccessToken("empty-interests@vocatio.com");

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "age": 18,
                                  "grade": "SECUNDARIA_4",
                                  "interests": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.interests[0]").value("Selecciona al menos un interés"));
    }

    @Test
    void shouldNormalizeAndDeduplicateInterests() throws Exception {
        String token = obtainAccessToken("normalize@vocatio.com");

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "age": 20,
                                  "grade": "universidad_3",
                                  "interests": [
                                    "  Tecnología  ",
                                    "Arte",
                                    "Tecnología",
                                    "Arte",
                                    "TECNOLOGÍA"
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.grade").value("UNIVERSIDAD_3"))
                .andExpect(jsonPath("$.profile.interests", contains("Tecnología", "Arte")));
    }

    /** Registra y autentica un usuario de prueba. */
    private String obtainAccessToken(String email) throws Exception {
        register(email);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password1"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.path("tokens").path("accessToken").asText();
    }

    /** Crea el usuario de apoyo para las pruebas. */
    private void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password1"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }
}
