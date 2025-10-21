package com.acme.vocatio.service;

import com.acme.vocatio.exception.InvalidUrlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Servicio para validación de URLs y enlaces externos.
 */
@Service
@Slf4j
public class UrlValidationService {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    private static final int TIMEOUT_MS = 5000;

    /**
     * Valida si una URL tiene un formato válido.
     * 
     * @param url URL a validar
     * @return true si la URL es válida, false en caso contrario
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            // Añadir protocolo si no lo tiene
            String urlToValidate = url.startsWith("http") ? url : "https://" + url;
            
            // Validar formato con regex
            if (!URL_PATTERN.matcher(urlToValidate).matches()) {
                return false;
            }

            // Validar que se pueda crear una URL válida
            URI.create(urlToValidate).toURL();
            return true;
            
        } catch (Exception e) {
            log.debug("URL inválida: {}, Error: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Valida si una URL es accesible (responde HTTP 200).
     * 
     * @param url URL a validar
     * @return true si la URL es accesible, false en caso contrario
     */
    public boolean isUrlAccessible(String url) {
        if (!isValidUrl(url)) {
            return false;
        }

        try {
            String urlToCheck = url.startsWith("http") ? url : "https://" + url;
            URL urlObj = URI.create(urlToCheck).toURL();
            
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setInstanceFollowRedirects(true);
            
            // Establecer User-Agent para evitar bloqueos
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            return responseCode >= 200 && responseCode < 400;
            
        } catch (IOException | IllegalArgumentException e) {
            log.debug("URL no accesible: {}, Error: {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Valida una URL para enlace externo y lanza excepción si no es válida.
     * 
     * @param url URL a validar
     * @throws InvalidUrlException si la URL no es válida
     */
    public void validateExternalLink(String url) {
        if (!isValidUrl(url)) {
            throw new InvalidUrlException("Enlace no válido");
        }

        // Validación opcional de accesibilidad (puede ser costosa)
        // Se puede habilitar según los requerimientos
        /*
        if (!isUrlAccessible(url)) {
            throw new InvalidUrlException("El enlace no es accesible");
        }
        */
    }

    /**
     * Normaliza una URL añadiendo protocolo si es necesario.
     * 
     * @param url URL a normalizar
     * @return URL normalizada
     */
    public String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        
        String normalized = url.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        
        return normalized;
    }
}