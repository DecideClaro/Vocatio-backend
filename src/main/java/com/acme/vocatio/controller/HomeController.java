package com.acme.vocatio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "¡Bienvenido a Vocatio API! Visita /api/v1/swagger-ui/index.html para explorar los endpoints.";
    }
}