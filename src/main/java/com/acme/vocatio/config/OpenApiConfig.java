package com.acme.vocatio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vocatio - Plataforma de Orientación Vocacional API")
                        .version("1.0.0")
                        .description("""
                            API REST completa para la plataforma de orientación vocacional Vocatio.
                            
                            **Módulos disponibles:**
                            - **Autenticación**: Registro, login y gestión de usuarios
                            - **Recursos de Aprendizaje**: Gestión de materiales educativos
                            - **Carreras**: Exploración y recomendaciones vocacionales
                            - **Chatbot**: Asistente virtual de orientación
                            - **Perfiles**: Gestión de datos personales
                            
                            **Características:**
                            - Autenticación JWT
                            - Descarga de PDFs
                            - Validación de enlaces externos
                            - Gestión de favoritos
                            - Recomendaciones personalizadas
                            
                            ** Para usar la autenticación:**
                            1. Haz login en `/auth/login` para obtener el accessToken
                            2. Copia el accessToken de la respuesta
                            3. Haz clic en "Authorize" (🔒) en la parte superior
                            4. Pega el token en el formato: `Bearer tu_token_aqui`
                            5. Haz clic en "Authorize" y luego "Close"
                            """)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Vocatio")
                                .email("vocatio@acme.com")
                                .url("https://github.com/DecideClaro/Vocatio-backend")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositorio del Proyecto en GitHub")
                        .url("https://github.com/DecideClaro/Vocatio-backend"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Token - Obtén tu token desde /auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}


// http://localhost:8080/swagger-ui.html