# Guía de Migración - Vocatio Backend

## 📋 Resumen de la Migración

Esta guía te ayudará a integrar la nueva base de datos de Vocatio manteniendo la funcionalidad actual. La migración es **incremental y segura**, lo que significa que no se romperá nada existente.

## 🔄 Qué Cambia y Qué Se Mantiene

### ✅ SE MANTIENE (sin cambios):
- **Tabla Usuario**: Estructura básica (id, email, contraseña)
- **Sistema de Autenticación**: JWT, RefreshToken funcionan igual
- **API Endpoints actuales**: `/api/auth/*` y `/api/profile/*` siguen funcionando
- **Tests de Postman**: Tus scripts actuales seguirán funcionando

### 🆕 SE AGREGA (nuevo):
- **Sistema de Carreras**: Catálogo completo de carreras universitarias
- **Universidades y Modalidades**: Información institucional
- **Test Vocacional Avanzado**: Sistema completo de evaluación
- **Favoritos**: Usuarios pueden guardar carreras favoritas
- **Material Educativo**: Recursos de aprendizaje
- **Sistema de Logros**: Gamificación básica

## 🚀 Pasos para la Migración

## ⚡ Opción Rápida (Automática)

Si prefieres ejecutar todo automáticamente:

```powershell
# Desde PowerShell en la raíz del proyecto:
.\migrar-bd.ps1
```

El script hará todo por ti: backup, migración, verificación y datos de ejemplo.

---

## 🔧 Opción Manual (Paso a Paso)

### Paso 1: Backup de Seguridad (Docker)
```bash
# Asegúrate de que el contenedor esté corriendo
docker-compose up -d

# Haz backup desde el contenedor Docker
docker-compose exec postgres pg_dump -U postgres vocatio_db > backup_vocatio_$(Get-Date -Format "yyyyMMdd").sql

# O alternativamente (Windows PowerShell):
docker exec postgres_vocatio pg_dump -U postgres vocatio_db > backup_vocatio.sql
```

### Paso 2: Ejecutar Script de Migración (Docker)
```bash
# Opción 1: Copiar el script al contenedor y ejecutar
docker cp BD/DDL_PG_MIGRATION.sql postgres_vocatio:/tmp/
docker-compose exec postgres psql -U postgres -d vocatio_db -f /tmp/DDL_PG_MIGRATION.sql

# Opción 2: Ejecutar directamente (PowerShell)
Get-Content BD/DDL_PG_MIGRATION.sql | docker-compose exec -T postgres psql -U postgres -d vocatio_db

# Opción 3: Usar herramienta de BD (recomendado)
# Conectar con DBeaver/pgAdmin a localhost:5434 y ejecutar el script
```

### Paso 3: Verificar Migración (Docker)
```bash
# Conectar al contenedor y verificar las tablas
docker-compose exec postgres psql -U postgres -d vocatio_db

# Dentro de psql, ejecuta:
# \dt
# 
# Deberías ver las nuevas tablas:
# carrera, universidad, areainteres, duracion, modalidad
# testvocacional, testintento, logro, materialeducativo
# Y las tablas de relaciones (carreraarea, usuariofavoritos, etc.)

# Ejecutar script de verificación completa:
docker cp BD/VERIFICACION_MIGRACION.sql postgres_vocatio:/tmp/
docker-compose exec postgres psql -U postgres -d vocatio_db -f /tmp/VERIFICACION_MIGRACION.sql
```

### Paso 4: Configurar Hibernate
En tu `application.properties`, asegúrate de que esté configurado correctamente:

```properties
# Cambiar de 'create' o 'create-drop' a 'update' para preservar datos
spring.jpa.hibernate.ddl-auto=update
```

### Paso 5: Compilar y Ejecutar
```bash
# Compila el proyecto
./mvnw clean compile

# Ejecuta la aplicación
./mvnw spring-boot:run
```

## 🧪 Nuevos Endpoints Disponibles

Después de la migración, tendrás acceso a estos nuevos endpoints:

### Carreras
- `GET /api/carreras` - Listar todas las carreras
- `GET /api/carreras/{id}` - Obtener carrera por ID
- `GET /api/carreras?busqueda=medicina` - Buscar carreras
- `POST /api/carreras` - Crear nueva carrera (admin)

### Favoritos
- `GET /api/favoritos/mis-favoritos` - Obtener favoritos del usuario
- `POST /api/favoritos/agregar?idCarrera=1` - Agregar a favoritos
- `DELETE /api/favoritos/eliminar?idCarrera=1` - Eliminar de favoritos
- `GET /api/favoritos/es-favorita/1` - Verificar si es favorita

## 🔗 Compatibilidad con Postman

Tus scripts actuales de Postman seguirán funcionando porque:

1. **Endpoints de Auth**: No cambian (`/api/auth/login`, `/api/auth/register`)
2. **Endpoints de Profile**: No cambian (`/api/profile/*`)
3. **Estructura de respuestas**: Se mantienen iguales

### Nuevas Colecciones de Postman
Puedes agregar estas nuevas peticiones a tu colección:

```json
{
  "name": "Obtener Carreras",
  "request": {
    "method": "GET",
    "url": "{{baseUrl}}/api/carreras"
  }
}
```

```json
{
  "name": "Agregar Favorito",
  "request": {
    "method": "POST",
    "url": "{{baseUrl}}/api/favoritos/agregar?idCarrera=1",
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer {{token}}"
      }
    ]
  }
}
```

## 🔍 Verificación Post-Migración

### 1. Verificar que la autenticación sigue funcionando
```bash
# Prueba login (debería funcionar igual que antes)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "usuario@test.com", "password": "password"}'
```

### 2. Probar nuevas funcionalidades
```bash
# Listar carreras (nuevo endpoint)
curl -X GET http://localhost:8080/api/carreras

# Agregar datos de prueba
curl -X POST http://localhost:8080/api/carreras \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Ingeniería de Sistemas", "descripcionCorta": "Carrera de tecnología"}'
```

## ⚠️ Troubleshooting

### Si encuentras errores:

1. **Error de columnas duplicadas**: 
   - El script está diseñado para ser idempotente
   - Puedes ejecutarlo múltiples veces sin problemas

2. **Error de compilación Java**:
   ```bash
   # Limpia y recompila
   ./mvnw clean compile
   ```

3. **Error de dependencias no encontradas**:
   - Verifica que todas las nuevas clases están en el classpath
   - Reinicia tu IDE

4. **Error en tests existentes**:
   - Los tests existentes deberían seguir funcionando
   - Si fallan, es posible que necesiten una actualización menor

### Rollback (si algo sale mal):
```bash
# Restaurar desde backup
psql -h localhost -p 5434 -U postgres -d vocatio_db < backup_vocatio_YYYYMMDD.sql

# Luego reinicia la aplicación con el código anterior
```

## 📊 Datos de Ejemplo

El script incluye algunos datos básicos. Para pruebas más completas, puedes insertar:

```sql
-- Insertar carreras de ejemplo
INSERT INTO Carrera (nombre, descripcion_corta, descripcion_larga) VALUES
('Ingeniería de Sistemas', 'Desarrollo de software y sistemas', 'Carrera enfocada en el desarrollo de aplicaciones...'),
('Medicina', 'Ciencias de la salud', 'Formación integral en ciencias médicas...'),
('Derecho', 'Ciencias jurídicas', 'Estudio del sistema legal y normativo...');

-- Insertar universidades de ejemplo
INSERT INTO Universidad (nombre_universidad, sitio_web) VALUES
('Universidad Nacional', 'https://unal.edu.co'),
('Universidad de los Andes', 'https://uniandes.edu.co'),
('Pontificia Universidad Javeriana', 'https://javeriana.edu.co');
```

## ✅ Lista de Verificación Final

- [ ] Backup realizado
- [ ] Script de migración ejecutado sin errores
- [ ] Aplicación compila correctamente
- [ ] Aplicación se ejecuta sin errores
- [ ] Endpoints existentes siguen funcionando
- [ ] Nuevos endpoints responden correctamente
- [ ] Tests de Postman pasan
- [ ] Datos de ejemplo insertados (opcional)

## 🎯 Próximos Pasos

Una vez que la migración esté completa, puedes:

1. **Desarrollar el frontend** para las nuevas funcionalidades
2. **Agregar más datos** de carreras y universidades
3. **Implementar el test vocacional** completo
4. **Agregar sistema de recomendaciones**
5. **Implementar gamificación** (logros, rachas)

---

**¡La migración está diseñada para ser segura y gradual!** Si tienes dudas en cualquier paso, no dudes en consultar.