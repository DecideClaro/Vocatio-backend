package com.acme.vocatio.web;

import com.acme.vocatio.exception.CareerNotFoundException;
import com.acme.vocatio.exception.DuplicateEmailException;
import com.acme.vocatio.exception.InvalidAccountDeletionConfirmationException;
import com.acme.vocatio.exception.InvalidCredentialsException;
import com.acme.vocatio.exception.InvalidCurrentPasswordException;
import com.acme.vocatio.exception.InvalidPersonalDataException;
import com.acme.vocatio.exception.InvalidPasswordChangeException;
import com.acme.vocatio.exception.PasswordChangeRateLimitException;
import com.acme.vocatio.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maneja errores comunes de la API. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Devuelve errores de validación de payloads. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField, Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Revise los datos enviados");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    /** Convierte violaciones a una respuesta legible. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolations(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Revise los datos enviados");
        body.put(
                "errors",
                ex.getConstraintViolations().stream()
                        .collect(Collectors.groupingBy(
                                violation -> violation.getPropertyPath().toString(),
                                Collectors.mapping(violation -> violation.getMessage(), Collectors.toList()))));
        return ResponseEntity.badRequest().body(body);
    }

    /** Ofrece sugerencias cuando el correo ya existe. */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put(
                "suggestions",
                List.of("Inicia sesión con tu cuenta", "Recupera tu contraseña si la olvidaste"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** Informa credenciales inválidas. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /** Error cuando la confirmación de borrado no es válida. */
    @ExceptionHandler(InvalidAccountDeletionConfirmationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDeletionConfirmation(
            InvalidAccountDeletionConfirmationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Contraseña actual incorrecta al intentar actualizarla. */
    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCurrentPassword(InvalidCurrentPasswordException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Indica errores al actualizar datos personales. */
    @ExceptionHandler(InvalidPersonalDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPersonalData(InvalidPersonalDataException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /** Error cuando la contraseña nueva no cumple la política. */
    @ExceptionHandler(InvalidPasswordChangeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPasswordChange(InvalidPasswordChangeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /** Indica que se alcanzó el límite de intentos de cambio de contraseña. */
    @ExceptionHandler(PasswordChangeRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordChangeRateLimit(PasswordChangeRateLimitException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("retryAfterSeconds", ex.retryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    /** Avisa cuando no se encontró al usuario. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** Respuesta consistente cuando una carrera no existe. */
    @ExceptionHandler(CareerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCareerNotFound(CareerNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
