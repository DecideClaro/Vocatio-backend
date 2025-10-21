package com.acme.vocatio.service;

import com.acme.vocatio.exception.FileNotDownloadableException;
import com.acme.vocatio.exception.InvalidUrlException;
import com.acme.vocatio.exception.PdfValidationException;
import com.acme.vocatio.model.LearningResource;
import com.acme.vocatio.model.TipoRecurso;
import com.acme.vocatio.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Servicio para manejo de descarga de archivos PDF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfDownloadService {

    private final LearningResourceRepository learningResourceRepository;
    private final UrlValidationService urlValidationService;

    /**
     * Descarga un archivo PDF desde la URL del recurso.
     * 
     * @param resourceId ID del recurso de aprendizaje
     * @return ResponseEntity con el archivo PDF
     * @throws FileNotDownloadableException si el archivo no puede ser descargado
     * @throws PdfValidationException si el archivo no es un PDF válido
     */
    public ResponseEntity<Resource> downloadPdf(Long resourceId) {
        log.info("Iniciando descarga de PDF para recurso ID: {}", resourceId);
        
        LearningResource resource = learningResourceRepository.findById(resourceId)
            .orElseThrow(() -> new FileNotDownloadableException("Recurso no encontrado"));

        // Validar que sea un recurso PDF
        if (resource.getTipoRecurso() != TipoRecurso.pdf) {
            throw new FileNotDownloadableException("El recurso no es un archivo PDF descargable");
        }

        // Validar URL
        if (!urlValidationService.isValidUrl(resource.getUrlRecurso())) {
            throw new InvalidUrlException("La URL del recurso no es válida");
        }

        try {
            URL url = URI.create(resource.getUrlRecurso()).toURL();
            
            // Validar que sea accesible
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().contains("pdf")) {
                throw new PdfValidationException("El archivo en la URL no es un PDF válido");
            }

            // Crear recurso para descarga
            Resource fileResource = new UrlResource(url);
            
            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new FileNotDownloadableException("No se puede acceder al archivo PDF");
            }

            // Generar nombre de archivo
            String filename = generatePdfFilename(resource);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileResource);

        } catch (MalformedURLException e) {
            log.error("URL malformada para recurso {}: {}", resourceId, resource.getUrlRecurso(), e);
            throw new InvalidUrlException("La URL del recurso no es válida");
        } catch (IOException e) {
            log.error("Error al acceder al archivo PDF para recurso {}: {}", resourceId, e.getMessage(), e);
            throw new FileNotDownloadableException("No se pudo acceder al archivo PDF: " + e.getMessage());
        }
    }

    /**
     * Genera un nombre de archivo apropiado para el PDF.
     */
    private String generatePdfFilename(LearningResource resource) {
        String baseFilename = resource.getArchivoPdf();
        if (baseFilename != null && !baseFilename.trim().isEmpty()) {
            return baseFilename.endsWith(".pdf") ? baseFilename : baseFilename + ".pdf";
        }
        
        // Si no hay nombre de archivo, usar el título
        String titulo = resource.getTitulo().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        return titulo.replaceAll("\\s+", "_") + ".pdf";
    }

    /**
     * Valida si un recurso es descargable.
     */
    public boolean isResourceDownloadable(Long resourceId) {
        return learningResourceRepository.findById(resourceId)
            .map(resource -> resource.getTipoRecurso() == TipoRecurso.pdf)
            .orElse(false);
    }
}