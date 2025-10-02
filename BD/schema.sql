-- =============================================================================
-- Generado: 2024-09-30
-- PostgreSQL 16
-- =============================================================================

-- Crear la base de datos (ejecutar por separado si es necesario)
-- CREATE DATABASE vocatio_db;
-- \c vocatio_db;

-- =============================================================================
-- MÓDULO 1: AUTENTICACIÓN Y USUARIOS
-- =============================================================================

-- Tabla principal de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Tabla Usuario (ALIAS/VISTA para compatibilidad futura)
-- Permite que el código futuro use "Usuario" mientras el actual usa "users"
CREATE VIEW Usuario AS
SELECT
    id as id_usuario,
    email,
    password_hash as contrasena_hash,
    created_at as fecha_creacion,
    NULL as token_recuperacion,
    NULL as token_expiracion,
    CASE WHEN is_active THEN 'activo' ELSE 'inactivo' END as estado_cuenta
FROM users;

-- Tabla de perfiles
CREATE TABLE profiles (
    id_usuario BIGINT PRIMARY KEY,
    name VARCHAR(255),
    age SMALLINT,
    grade VARCHAR(64), -- AcademicGrade enum
    personal_interests JSONB, -- Lista de intereses en formato JSON
    CONSTRAINT fk_profiles_user FOREIGN KEY (id_usuario) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla PerfilUsuario
CREATE TABLE PerfilUsuario (
    id_usuario BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    fecha_nacimiento DATE,
    pais VARCHAR(100),
    nivel_educativo VARCHAR(100),
    avatar_url VARCHAR(255),
    ultimo_login TIMESTAMP,
    racha_actual INT DEFAULT 0,
    ultima_fecha_actividad DATE
);

-- Tabla de tokens de refresco
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla RefreshToken
CREATE VIEW RefreshToken AS
SELECT
    id,
    user_id as id_usuario,
    token as token_hash,
    expires_at as fecha_expiracion,
    created_at as fecha_creacion,
    false as revocado
FROM refresh_tokens;

-- =============================================================================
-- MÓDULO 2: CATÁLOGO DE CARRERAS
-- =============================================================================

-- Tabla de carreras (MANTIENE nombres actuales)
CREATE TABLE carrera (
    id_carrera BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion_corta TEXT NOT NULL,
    descripcion_larga TEXT,
    plan_de_estudios TEXT,
    salida_laboral TEXT,
    imagen_url VARCHAR(255)
);

-- Alias para compatibilidad
CREATE VIEW Carrera AS SELECT * FROM carrera;

-- Tabla de universidades
CREATE TABLE universidad (
    id_universidad BIGSERIAL PRIMARY KEY,
    nombre_universidad VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255),
    sitio_web VARCHAR(255)
);

CREATE VIEW Universidad AS SELECT * FROM universidad;

-- Tabla de áreas de interés
CREATE TABLE areaInteres (
    id_area BIGSERIAL PRIMARY KEY,
    nombre_area VARCHAR(150) NOT NULL UNIQUE
);

CREATE VIEW AreaInteres AS SELECT * FROM areaInteres;

-- Tabla de duraciones
CREATE TABLE duracion (
    id_duracion BIGSERIAL PRIMARY KEY,
    rango_duracion VARCHAR(50) NOT NULL UNIQUE
);

CREATE VIEW Duracion AS SELECT * FROM duracion;

-- Tabla de modalidades
CREATE TABLE modalidad (
    id_modalidad BIGSERIAL PRIMARY KEY,
    nombre_modalidad VARCHAR(100) NOT NULL UNIQUE
);

CREATE VIEW Modalidad AS SELECT * FROM modalidad;

-- =============================================================================
-- MÓDULO 3: RELACIONES CATÁLOGO
-- =============================================================================

CREATE TABLE carreraArea (
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    id_area BIGINT NOT NULL REFERENCES areaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_area)
);

CREATE VIEW CarreraArea AS SELECT * FROM carreraArea;

CREATE TABLE carreraDuracion (
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    id_duracion BIGINT NOT NULL REFERENCES duracion(id_duracion) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_duracion)
);

CREATE VIEW CarreraDuracion AS SELECT * FROM carreraDuracion;

CREATE TABLE carreraModalidad (
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    id_modalidad BIGINT NOT NULL REFERENCES modalidad(id_modalidad) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_modalidad)
);

CREATE VIEW CarreraModalidad AS SELECT * FROM carreraModalidad;

CREATE TABLE carreraUniversidad (
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    id_universidad BIGINT NOT NULL REFERENCES universidad(id_universidad) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_universidad)
);

CREATE VIEW CarreraUniversidad AS SELECT * FROM carreraUniversidad;

-- =============================================================================
-- MÓDULO 4: SISTEMA DE FAVORITOS
-- =============================================================================

-- Tabla de favoritos (MANTIENE nombre actual)
CREATE TABLE usuariofavoritos (
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    fecha_agregado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_carrera)
);

-- Alias para compatibilidad futura
CREATE VIEW UsuarioFavoritos AS SELECT * FROM usuariofavoritos;

-- =============================================================================
-- MÓDULO 5: SISTEMA DE TEST VOCACIONAL
-- =============================================================================

-- Tabla de tests vocacionales (MANTIENE nombre actual)
CREATE TABLE testvocacional (
    id_test_vocacional BIGSERIAL PRIMARY KEY,
    nombre_test VARCHAR(255) NOT NULL,
    descripcion TEXT,
    version_test VARCHAR(50)
);

CREATE VIEW TestVocacional AS SELECT * FROM testvocacional;

-- Tabla de preguntas (NUEVA)
CREATE TABLE pregunta (
    id_pregunta BIGSERIAL PRIMARY KEY,
    id_test_vocacional BIGINT NOT NULL REFERENCES testvocacional(id_test_vocacional) ON DELETE CASCADE,
    texto_pregunta TEXT NOT NULL,
    orden INT
);

CREATE VIEW Pregunta AS SELECT * FROM pregunta;

-- Tabla de opciones (NUEVA)
CREATE TABLE opcion (
    id_opcion BIGSERIAL PRIMARY KEY,
    id_pregunta BIGINT NOT NULL REFERENCES pregunta(id_pregunta) ON DELETE CASCADE,
    texto_opcion TEXT NOT NULL,
    area_asociada VARCHAR(150)
);

CREATE VIEW Opcion AS SELECT * FROM opcion;

-- Tabla de intentos de test (MANTIENE nombre actual)
CREATE TABLE testintento (
    id_intento BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_test_vocacional BIGINT NOT NULL REFERENCES testvocacional(id_test_vocacional) ON DELETE CASCADE,
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP,
    estado VARCHAR(50) NOT NULL DEFAULT 'en_progreso',
    es_visible BOOLEAN NOT NULL DEFAULT true
);

CREATE VIEW TestIntento AS SELECT * FROM testintento;

-- Nuevas tablas para el sistema completo de test
CREATE TABLE respuestaUsuario (
    id_respuesta BIGSERIAL PRIMARY KEY,
    id_intento BIGINT NOT NULL REFERENCES testintento(id_intento) ON DELETE CASCADE,
    id_pregunta BIGINT NOT NULL REFERENCES pregunta(id_pregunta),
    id_opcion_seleccionada BIGINT NOT NULL REFERENCES opcion(id_opcion),
    fecha_respuesta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW RespuestaUsuario AS SELECT * FROM respuestaUsuario;

CREATE TABLE resultadoTest (
    id_resultado BIGSERIAL PRIMARY KEY,
    id_intento BIGINT NOT NULL UNIQUE REFERENCES testintento(id_intento) ON DELETE CASCADE,
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW ResultadoTest AS SELECT * FROM resultadoTest;

CREATE TABLE resultadoPorArea (
    id_resultado_area BIGSERIAL PRIMARY KEY,
    id_resultado BIGINT NOT NULL REFERENCES resultadoTest(id_resultado) ON DELETE CASCADE,
    nombre_area VARCHAR(150) NOT NULL,
    puntuacion DECIMAL(5, 2) NOT NULL
);

CREATE VIEW ResultadoPorArea AS SELECT * FROM resultadoPorArea;

-- =============================================================================
-- MÓDULO 6: MATERIAL EDUCATIVO
-- =============================================================================

CREATE TABLE materialeducativo (
    id_material BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo_material VARCHAR(50) NOT NULL,
    url_contenido VARCHAR(255) NOT NULL,
    imagen_preview_url VARCHAR(255)
);

CREATE VIEW MaterialEducativo AS SELECT * FROM materialeducativo;

CREATE TABLE materialCarrera (
    id_material BIGINT NOT NULL REFERENCES materialeducativo(id_material) ON DELETE CASCADE,
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    PRIMARY KEY (id_material, id_carrera)
);

CREATE VIEW MaterialCarrera AS SELECT * FROM materialCarrera;

CREATE TABLE materialArea (
    id_material BIGINT NOT NULL REFERENCES materialeducativo(id_material) ON DELETE CASCADE,
    id_area BIGINT NOT NULL REFERENCES areaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_material, id_area)
);

CREATE VIEW MaterialArea AS SELECT * FROM materialArea;

CREATE TABLE usuarioMaterialFavorito (
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_material BIGINT NOT NULL REFERENCES materialeducativo(id_material) ON DELETE CASCADE,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_material)
);

CREATE VIEW UsuarioMaterialFavorito AS SELECT * FROM usuarioMaterialFavorito;

-- =============================================================================
-- MÓDULO 7: SISTEMA DE LOGROS Y ENGAGEMENT
-- =============================================================================

CREATE TABLE logro (
    id_logro BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    icono VARCHAR(100) NOT NULL
);

CREATE VIEW Logro AS SELECT * FROM logro;

CREATE TABLE usuarioLogro (
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_logro BIGINT NOT NULL REFERENCES logro(id_logro) ON DELETE CASCADE,
    fecha_obtenido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_logro)
);

CREATE VIEW UsuarioLogro AS SELECT * FROM usuarioLogro;

CREATE TABLE usuarioActividad (
    id_actividad BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo_actividad VARCHAR(100) NOT NULL,
    fecha_actividad TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE VIEW UsuarioActividad AS SELECT * FROM usuarioActividad;

CREATE TABLE usuarioRecomendaciones (
    id_recomendacion BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_carrera BIGINT NOT NULL REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    id_resultado_test BIGINT NULL REFERENCES resultadoTest(id_resultado) ON DELETE SET NULL,
    fecha_recomendacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    es_activa BOOLEAN DEFAULT TRUE
);

CREATE VIEW UsuarioRecomendaciones AS SELECT * FROM usuarioRecomendaciones;

CREATE TABLE usuarioInteres (
    id_usuario BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_area BIGINT NOT NULL REFERENCES areaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_usuario, id_area)
);

CREATE VIEW UsuarioInteres AS SELECT * FROM usuarioInteres;

-- =============================================================================
-- ÍNDICES PARA OPTIMIZAR PERFORMANCE
-- =============================================================================

-- Índices para autenticación (código actual)
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Índices para perfiles
CREATE INDEX idx_profiles_age ON profiles(age);
CREATE INDEX idx_profiles_grade ON profiles(grade);
CREATE INDEX idx_perfil_usuario_nivel ON PerfilUsuario(nivel_educativo);

-- Índices para carreras y búsquedas
CREATE INDEX idx_carrera_nombre ON carrera(nombre);
CREATE INDEX idx_universidad_nombre ON universidad(nombre_universidad);
CREATE INDEX idx_area_nombre ON areaInteres(nombre_area);

-- Índices para favoritos
CREATE INDEX idx_favoritos_usuario ON usuariofavoritos(id_usuario);
CREATE INDEX idx_favoritos_carrera ON usuariofavoritos(id_carrera);
CREATE INDEX idx_favoritos_fecha ON usuariofavoritos(fecha_agregado);

-- Índices para tests vocacionales
CREATE INDEX idx_test_intento_usuario ON testintento(id_usuario);
CREATE INDEX idx_test_intento_estado ON testintento(estado);
CREATE INDEX idx_test_intento_fecha ON testintento(fecha_inicio);
CREATE INDEX idx_respuesta_intento ON respuestaUsuario(id_intento);
CREATE INDEX idx_pregunta_test ON pregunta(id_test_vocacional);
CREATE INDEX idx_opcion_pregunta ON opcion(id_pregunta);

-- Índices para relaciones many-to-many
CREATE INDEX idx_carrera_area_carrera ON carreraArea(id_carrera);
CREATE INDEX idx_carrera_area_area ON carreraArea(id_area);
CREATE INDEX idx_carrera_duracion_carrera ON carreraDuracion(id_carrera);
CREATE INDEX idx_carrera_modalidad_carrera ON carreraModalidad(id_carrera);
CREATE INDEX idx_carrera_universidad_carrera ON carreraUniversidad(id_carrera);

-- Índices para engagement
CREATE INDEX idx_usuario_logro_usuario ON usuarioLogro(id_usuario);
CREATE INDEX idx_usuario_actividad_usuario ON usuarioActividad(id_usuario);
CREATE INDEX idx_usuario_actividad_fecha ON usuarioActividad(fecha_actividad);
CREATE INDEX idx_usuario_recomendaciones_usuario ON usuarioRecomendaciones(id_usuario);
CREATE INDEX idx_usuario_recomendaciones_activa ON usuarioRecomendaciones(es_activa);

-- Índices para material educativo
CREATE INDEX idx_material_carrera_carrera ON materialCarrera(id_carrera);
CREATE INDEX idx_material_area_area ON materialArea(id_area);
CREATE INDEX idx_usuario_material_favorito_usuario ON usuarioMaterialFavorito(id_usuario);

-- =============================================================================
-- DATOS INICIALES BÁSICOS
-- =============================================================================

-- Insertar áreas de interés
INSERT INTO areaInteres (nombre_area) VALUES
('Tecnología'),
('Ciencias'),
('Arte'),
('Deportes'),
('Música'),
('Literatura'),
('Matemáticas'),
('Historia'),
('Idiomas'),
('Negocios'),
('Medicina'),
('Ingeniería'),
('Educación'),
('Derecho'),
('Comunicaciones')
ON CONFLICT (nombre_area) DO NOTHING;

-- Insertar duraciones típicas
INSERT INTO duracion (rango_duracion) VALUES
('2-3 años'),
('4-5 años'),
('5-6 años'),
('6+ años')
ON CONFLICT (rango_duracion) DO NOTHING;

-- Insertar modalidades comunes
INSERT INTO modalidad (nombre_modalidad) VALUES
('Presencial'),
('Virtual'),
('Semipresencial'),
('Híbrida')
ON CONFLICT (nombre_modalidad) DO NOTHING;

-- Test vocacional básico
INSERT INTO testvocacional (nombre_test, descripcion, version_test) VALUES
('Test de Orientación Vocacional Básico', 'Evaluación inicial de intereses y aptitudes', '1.0')
ON CONFLICT DO NOTHING;

