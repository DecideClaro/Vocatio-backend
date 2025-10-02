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

-- Índices para autenticación y perfiles
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_profiles_age ON profiles(age);
CREATE INDEX idx_profiles_grade ON profiles(grade);
CREATE INDEX idx_perfil_usuario_nivel ON PerfilUsuario(nivel_educativo);