# Base de Datos - Vocatio Backend

## 📋 Descripción General

Este directorio contiene la documentación y datos de ejemplo para la base de datos PostgreSQL de Vocatio, una plataforma de orientación vocacional desarrollada con Spring Boot.

## 🚀 Configuración y Ejecución

### Prerrequisitos
- Docker y Docker Compose instalados
- PostgreSQL (a través de Docker)

### Inicialización
```bash
# Iniciar la base de datos
docker-compose up -d

# La aplicación Spring Boot creará automáticamente las tablas
./mvnw spring-boot:run
```

## 🗂️ Estructura de la Base de Datos

### 📊 Diagrama de la Base de Datos

![Esquema de Base de Datos - Vocatio](https://dbdiagram.io/embed/68db0f25d2b621e4227ac776)

*Diagrama completo de la estructura de la base de datos con todas las relaciones entre tablas*

> **💡 Tip**: Puedes ver el diagrama interactivo completo en [dbdiagram.io](https://dbdiagram.io/d/Dirama-68db0f25d2b621e4227ac776)

**📋 Tablas incluidas en el esquema:**
- **Usuario** → Gestión de usuarios y autenticación
- **Perfil** → Información personal y académica  
- **Carrera** → Catálogo de carreras universitarias
- **Test Vocacional** → Sistema de evaluación vocacional
- **Favoritos** → Carreras marcadas como favoritas
- **Material Educativo** → Recursos de aprendizaje
- **Universidad** → Información de instituciones educativas

### Tablas Principales

#### 👤 **users**
Tabla principal de usuarios del sistema.
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT true
);
```

#### 👤 **profiles**
Perfil detallado de cada usuario (relación 1:1 con users).
```sql
CREATE TABLE profiles (
    id_usuario BIGINT PRIMARY KEY REFERENCES users(id),
    name VARCHAR(255),
    age SMALLINT,
    grade VARCHAR(64), -- AcademicGrade enum
    personal_interests JSONB -- Lista de intereses en formato JSON
);
```

#### 🔐 **refresh_tokens**
Tokens de refresco para autenticación JWT.
```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### 🎓 **carrera**
Catálogo de carreras universitarias disponibles.
```sql
CREATE TABLE carrera (
    id_carrera BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion_corta TEXT NOT NULL,
    descripcion_larga TEXT,
    plan_de_estudios TEXT,
    salida_laboral TEXT,
    imagen_url VARCHAR(255)
);
```

#### ⭐ **usuario_favoritos**
Relación many-to-many entre usuarios y carreras favoritas.
```sql
CREATE TABLE usuario_favoritos (
    id_usuario BIGINT NOT NULL REFERENCES users(id),
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera),
    fecha_agregado TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_usuario, id_carrera)
);
```

#### 📝 **test_intento**
Intentos de test vocacional realizados por los usuarios.
```sql
CREATE TABLE test_intento (
    id BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES users(id),
    fecha_inicio TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_fin TIMESTAMP,
    completado BOOLEAN NOT NULL DEFAULT false
);
```

## 🔗 Relaciones de la Base de Datos

```
┌─────────────┐    1:1    ┌─────────────┐
│    users    │ ────────► │  profiles   │
└─────────────┘           └─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐
│refresh_tokens│
└─────────────┘

┌─────────────┐    N:M    ┌─────────────┐
│    users    │ ◄────────► │   carrera   │
└─────────────┘  favoritos └─────────────┘

┌─────────────┐    1:N    ┌─────────────┐
│    users    │ ────────► │test_intento │
└─────────────┘           └─────────────┘
```


## 📊 Enumeraciones y Tipos de Datos

### AcademicGrade (Grado Académico)
```java
public enum AcademicGrade {
    SECUNDARIA_1, SECUNDARIA_2, SECUNDARIA_3, 
    SECUNDARIA_4, SECUNDARIA_5,
    PREUNIVERSITARIO,
    SUPERIOR_TECNICA_1, SUPERIOR_TECNICA_2, SUPERIOR_TECNICA_3,
    UNIVERSIDAD_1, UNIVERSIDAD_2, UNIVERSIDAD_3, 
    UNIVERSIDAD_4, UNIVERSIDAD_5,
    EGRESADO, BACHILLER, LICENCIADO, 
    MAGISTER, DOCTOR
}
```

## 🗄️ Archivos Incluidos

- **`schema.sql`**: Script DDL completo para recrear la base de datos desde cero
- **`DATOS_EJEMPLO.sql`**: Script con datos de ejemplo para desarrollo y testing
- **`README.md`**: Este archivo de documentación

## 🔧 Recrear la Base de Datos desde Cero

Si necesitas recrear completamente la base de datos (por corrupción, migración, etc.):

### **Opción 1: Usando el Script DDL**
```bash
# 1. Hacer backup de seguridad (recomendado)
docker-compose exec postgres pg_dump -U postgres vocatio_db > backup_$(date +%Y%m%d).sql

# 2. Conectar a PostgreSQL
docker-compose exec postgres psql -U postgres -d vocatio_db

# 3. Ejecutar el script de recreación
\i BD/schema.sql

# 4. Salir de psql
\q
```

### **Opción 2: Recreación Completa con Docker**
```bash
# 1. Parar y eliminar contenedores y volúmenes
docker-compose down -v

# 2. Recrear desde cero
docker-compose up -d

# 3. Esperar que PostgreSQL inicie completamente
docker-compose logs -f postgres

# 4. Aplicar schema
docker-compose exec postgres psql -U postgres -d vocatio_db -f BD/schema.sql

# 5. Iniciar la aplicación Spring Boot
./mvnw spring-boot:run
```

### **Verificación Post-Recreación:**
```bash
# Verificar tablas creadas
docker-compose exec postgres psql -U postgres -d vocatio_db -c "\dt"

# Verificar estructura de usuarios
docker-compose exec postgres psql -U postgres -d vocatio_db -c "\d users"

# Probar la aplicación
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@recreacion.com","password":"Password1","rememberMe":false}'
```
