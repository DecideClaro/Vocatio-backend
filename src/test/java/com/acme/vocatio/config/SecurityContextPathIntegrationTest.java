package com.acme.vocatio.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "server.servlet.context-path=/api/v1")
class SecurityContextPathIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpointsShouldRemainAccessibleUnderContextPath() throws Exception {
        String email = "context-path-" + UUID.randomUUID() + "@vocatio.com";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password1"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
