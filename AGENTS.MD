## Refactor Agent
Input: módulos con `@Service` o `@Controller` complejos.  
Acciones: dividir responsabilidades, añadir `@Transactional(readOnly = true)` donde aplique, usar records para DTOs.

## Test Agent
Input: clases sin cobertura > 0.8 JaCoCo.  
Acciones: generar tests JUnit5 + Testcontainers; meta +10 % cobertura.  

## Docs Agent
Input: merges a main.  
Acciones: actualizar `README.md` con curl examples, paths del Actuator y Swagger UI.  
