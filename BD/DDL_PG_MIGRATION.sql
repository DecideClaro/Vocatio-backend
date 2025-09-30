-- =============================================================================
-- SCRIPT DE MIGRACIÓN INCREMENTAL PARA VOCATIO
-- Este script actualiza la BD actual sin romper la funcionalidad existente
-- =============================================================================

-- =============================================================================
-- PASO 1: MIGRAR TABLAS EXISTENTES
-- =============================================================================

-- 1.1 Renombrar tabla Usuario para que coincida con el nuevo esquema
-- (Solo si no existe ya la tabla con el nuevo nombre)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuario' AND table_schema = 'public') THEN
        -- Renombrar la tabla users a Usuario
        ALTER TABLE IF EXISTS users RENAME TO Usuario;
        
        -- Renombrar las columnas para que coincidan con el nuevo esquema
        ALTER TABLE Usuario RENAME COLUMN id TO id_usuario;
        ALTER TABLE Usuario RENAME COLUMN created_at TO fecha_creacion;
        ALTER TABLE Usuario RENAME COLUMN is_active TO esta_activo;
        ALTER TABLE Usuario RENAME COLUMN password_hash TO contrasena_hash;
        
        -- Agregar nuevas columnas si no existen
        ALTER TABLE Usuario 
        ADD COLUMN IF NOT EXISTS token_recuperacion VARCHAR(255),
        ADD COLUMN IF NOT EXISTS token_expiracion TIMESTAMP,
        ADD COLUMN IF NOT EXISTS estado_cuenta VARCHAR(50) DEFAULT 'activo';
    END IF;
END $$;

-- 1.2 Migrar tabla Profile a PerfilUsuario
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'perfilusuario' AND table_schema = 'public') THEN
        -- Crear la nueva tabla PerfilUsuario basada en la actual profiles
        CREATE TABLE PerfilUsuario AS 
        SELECT 
            id_usuario,
            name as nombre,
            '' as apellido, -- Campo nuevo, inicialmente vacío
            NULL::DATE as fecha_nacimiento, -- Campo nuevo
            NULL as pais, -- Campo nuevo
            grade as nivel_educativo,
            NULL as avatar_url, -- Campo nuevo
            NULL::TIMESTAMP as ultimo_login, -- Campo nuevo
            0 as racha_actual, -- Campo nuevo
            NULL::DATE as ultima_fecha_actividad, -- Campo nuevo
            age -- Mantenemos age por compatibilidad
        FROM profiles;
        
        -- Agregar constraint de clave primaria
        ALTER TABLE PerfilUsuario ADD PRIMARY KEY (id_usuario);
        
        -- Agregar foreign key
        ALTER TABLE PerfilUsuario 
        ADD CONSTRAINT fk_perfilusuario_usuario 
        FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE;
        
        -- Mantener la tabla profiles para no romper la aplicación actual
        -- La eliminaremos en una fase posterior
    END IF;
END $$;

-- 1.3 Migrar tabla RefreshToken
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refresh_tokens' AND table_schema = 'public') THEN
        -- Renombrar columnas para que coincidan con el nuevo esquema
        ALTER TABLE refresh_tokens RENAME COLUMN user_id TO id_usuario;
        ALTER TABLE refresh_tokens RENAME COLUMN expires_at TO fecha_expiracion;
        ALTER TABLE refresh_tokens RENAME COLUMN created_at TO fecha_creacion;
        ALTER TABLE refresh_tokens RENAME COLUMN revoked TO revocado;
        
        -- Renombrar la tabla
        ALTER TABLE refresh_tokens RENAME TO RefreshToken;
    END IF;
END $$;

-- =============================================================================
-- PASO 2: CREAR NUEVAS TABLAS DEL SISTEMA EXPANDIDO
-- =============================================================================

-- 2.1 Sistema de Carreras y Universidades
CREATE TABLE IF NOT EXISTS Carrera (
    id_carrera SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion_corta TEXT NOT NULL,
    descripcion_larga TEXT NULL,
    plan_de_estudios TEXT NULL,
    salida_laboral TEXT NULL,
    imagen_url VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS Universidad (
    id_universidad SERIAL PRIMARY KEY,
    nombre_universidad VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255) NULL,
    sitio_web VARCHAR(255) NULL
);

-- 2.2 Actualizar AreaInteres (puede existir de antes)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'areainteres' AND table_schema = 'public') THEN
        -- Si no existe, crearla
        CREATE TABLE AreaInteres (
            id_area SERIAL PRIMARY KEY,
            nombre_area VARCHAR(150) NOT NULL UNIQUE
        );
    ELSE
        -- Si existe, asegurar que tiene la columna correcta
        ALTER TABLE AreaInteres RENAME COLUMN nombre TO nombre_area;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS Duracion (
    id_duracion SERIAL PRIMARY KEY,
    rango_duracion VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Modalidad (
    id_modalidad SERIAL PRIMARY KEY,
    nombre_modalidad VARCHAR(100) NOT NULL UNIQUE
);

-- 2.3 Tablas de relaciones
CREATE TABLE IF NOT EXISTS CarreraArea (
    id_carrera INT REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    id_area INT REFERENCES AreaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_area)
);

CREATE TABLE IF NOT EXISTS CarreraDuracion (
    id_carrera INT REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    id_duracion INT REFERENCES Duracion(id_duracion) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_duracion)
);

CREATE TABLE IF NOT EXISTS CarreraModalidad (
    id_carrera INT REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    id_modalidad INT REFERENCES Modalidad(id_modalidad) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_modalidad)
);

CREATE TABLE IF NOT EXISTS CarreraUniversidad (
    id_carrera INT REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    id_universidad INT REFERENCES Universidad(id_universidad) ON DELETE CASCADE,
    PRIMARY KEY (id_carrera, id_universidad)
);

-- =============================================================================
-- PASO 3: SISTEMA DE TEST VOCACIONAL AVANZADO
-- =============================================================================

-- 3.1 Test Vocacional principal
CREATE TABLE IF NOT EXISTS TestVocacional (
    id_test_vocacional SERIAL PRIMARY KEY,
    nombre_test VARCHAR(255) NOT NULL,
    descripcion TEXT,
    version_test VARCHAR(50)
);

-- 3.2 Migrar sistema de preguntas existente si es necesario
DO $$
BEGIN
    -- Si existe la tabla Pregunta antigua, crear la nueva
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pregunta' AND table_schema = 'public') THEN
        CREATE TABLE Pregunta (
            id_pregunta SERIAL PRIMARY KEY,
            id_test_vocacional INT NOT NULL REFERENCES TestVocacional(id_test_vocacional) ON DELETE CASCADE,
            texto_pregunta TEXT NOT NULL,
            orden INT
        );
    END IF;
END $$;

-- 3.3 Actualizar tabla Opcion
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'opcion' AND table_schema = 'public') THEN
        CREATE TABLE Opcion (
            id_opcion SERIAL PRIMARY KEY,
            id_pregunta INT NOT NULL REFERENCES Pregunta(id_pregunta) ON DELETE CASCADE,
            texto_opcion TEXT NOT NULL,
            area_asociada VARCHAR(150) 
        );
    END IF;
END $$;

-- 3.4 Sistema de intentos de test
CREATE TABLE IF NOT EXISTS TestIntento (
    id_intento SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_test_vocacional INT NOT NULL REFERENCES TestVocacional(id_test_vocacional) ON DELETE CASCADE,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'en_progreso',
    es_visible BOOLEAN DEFAULT TRUE
);

-- 3.5 Respuestas de usuario
CREATE TABLE IF NOT EXISTS RespuestaUsuario (
    id_respuesta SERIAL PRIMARY KEY,
    id_intento INT NOT NULL REFERENCES TestIntento(id_intento) ON DELETE CASCADE,
    id_pregunta INT NOT NULL REFERENCES Pregunta(id_pregunta),
    id_opcion_seleccionada INT NOT NULL REFERENCES Opcion(id_opcion),
    fecha_respuesta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3.6 Resultados del test
CREATE TABLE IF NOT EXISTS ResultadoTest (
    id_resultado SERIAL PRIMARY KEY,
    id_intento INT NOT NULL UNIQUE REFERENCES TestIntento(id_intento) ON DELETE CASCADE,
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ResultadoPorArea (
    id_resultado_area SERIAL PRIMARY KEY,
    id_resultado INT NOT NULL REFERENCES ResultadoTest(id_resultado) ON DELETE CASCADE,
    nombre_area VARCHAR(150) NOT NULL,
    puntuacion DECIMAL(5, 2) NOT NULL
);

-- =============================================================================
-- PASO 4: SISTEMA DE ENGAGEMENT Y PERSONALIZACIÓN
-- =============================================================================

CREATE TABLE IF NOT EXISTS Logro (
    id_logro SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    icono VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS UsuarioLogro (
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_logro INT NOT NULL REFERENCES Logro(id_logro) ON DELETE CASCADE,
    fecha_obtenido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_logro)
);

CREATE TABLE IF NOT EXISTS UsuarioActividad (
    id_actividad SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    tipo_actividad VARCHAR(100) NOT NULL,
    fecha_actividad TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS UsuarioFavoritos (
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_carrera INT NOT NULL REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_carrera)
);

CREATE TABLE IF NOT EXISTS UsuarioRecomendaciones (
    id_recomendacion SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_carrera INT NOT NULL REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    id_resultado_test INT NULL REFERENCES ResultadoTest(id_resultado) ON DELETE SET NULL,
    fecha_recomendacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    es_activa BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS UsuarioInteres (
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_area INT NOT NULL REFERENCES AreaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_usuario, id_area)
);

-- =============================================================================
-- PASO 5: SISTEMA DE MATERIAL EDUCATIVO
-- =============================================================================

CREATE TABLE IF NOT EXISTS MaterialEducativo (
    id_material SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo_material VARCHAR(50) NOT NULL,
    url_contenido VARCHAR(255) NOT NULL,
    imagen_preview_url VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS MaterialCarrera (
    id_material INT NOT NULL REFERENCES MaterialEducativo(id_material) ON DELETE CASCADE,
    id_carrera INT NOT NULL REFERENCES Carrera(id_carrera) ON DELETE CASCADE,
    PRIMARY KEY (id_material, id_carrera)
);

CREATE TABLE IF NOT EXISTS MaterialArea (
    id_material INT NOT NULL REFERENCES MaterialEducativo(id_material) ON DELETE CASCADE,
    id_area INT NOT NULL REFERENCES AreaInteres(id_area) ON DELETE CASCADE,
    PRIMARY KEY (id_material, id_area)
);

CREATE TABLE IF NOT EXISTS UsuarioMaterialFavorito (
    id_usuario INT NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    id_material INT NOT NULL REFERENCES MaterialEducativo(id_material) ON DELETE CASCADE,
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_material)
);

-- =============================================================================
-- PASO 6: CREAR ÍNDICES PARA MEJORAR EL RENDIMIENTO
-- =============================================================================

-- Índices para tablas existentes migradas
CREATE INDEX IF NOT EXISTS idx_usuario_email ON Usuario(email);
CREATE INDEX IF NOT EXISTS idx_refreshtoken_usuario ON RefreshToken(id_usuario);
CREATE INDEX IF NOT EXISTS idx_perfilusuario_usuario ON PerfilUsuario(id_usuario);

-- Índices para nuevas tablas
CREATE INDEX IF NOT EXISTS idx_testintento_usuario ON TestIntento(id_usuario);
CREATE INDEX IF NOT EXISTS idx_respuesta_intento ON RespuestaUsuario(id_intento);
CREATE INDEX IF NOT EXISTS idx_carreraarea_carrera ON CarreraArea(id_carrera);
CREATE INDEX IF NOT EXISTS idx_carreraarea_area ON CarreraArea(id_area);
CREATE INDEX IF NOT EXISTS idx_usuariologro_usuario ON UsuarioLogro(id_usuario);
CREATE INDEX IF NOT EXISTS idx_usuariofavoritos_usuario ON UsuarioFavoritos(id_usuario);
CREATE INDEX IF NOT EXISTS idx_usuariorecomendaciones_usuario ON UsuarioRecomendaciones(id_usuario);
CREATE INDEX IF NOT EXISTS idx_usuariointeres_usuario ON UsuarioInteres(id_usuario);
CREATE INDEX IF NOT EXISTS idx_materialcarrera_carrera ON MaterialCarrera(id_carrera);
CREATE INDEX IF NOT EXISTS idx_materialarea_area ON MaterialArea(id_area);
CREATE INDEX IF NOT EXISTS idx_usuariomaterialfavorito_usuario ON UsuarioMaterialFavorito(id_usuario);

-- =============================================================================
-- PASO 7: DATOS DE EJEMPLO PARA TESTING
-- =============================================================================

-- Insertar datos básicos para testing
INSERT INTO TestVocacional (nombre_test, descripcion, version_test) 
VALUES ('Test Vocacional Básico', 'Evaluación de intereses profesionales', '1.0')
ON CONFLICT DO NOTHING;

INSERT INTO Duracion (rango_duracion) VALUES 
('1-2 años'), ('3-4 años'), ('5-6 años'), ('Más de 6 años')
ON CONFLICT (rango_duracion) DO NOTHING;

INSERT INTO Modalidad (nombre_modalidad) VALUES 
('Presencial'), ('Virtual'), ('Semipresencial'), ('A distancia')
ON CONFLICT (nombre_modalidad) DO NOTHING;

-- =============================================================================
-- FIN DEL SCRIPT DE MIGRACIÓN
-- =============================================================================

COMMIT;