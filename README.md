# Vocatio Backend 🎓

## 📋 Descripción del Proyecto

Vocatio es una plataforma de orientación vocacional desarrollada con Spring Boot que ayuda a estudiantes a descubrir carreras universitarias alineadas con sus intereses y habilidades.

## 🚀 Características Principales

- **Autenticación JWT completa** con refresh tokens
- **Gestión de perfiles de usuario** con validaciones robustas
- **Base de datos PostgreSQL** con Docker
- **API REST documentada** y probada con Postman
- **Validaciones de seguridad** avanzadas
- **Sistema de test vocacional** (en desarrollo)

## 🛠️ Tecnologías Utilizadas

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Base de Datos**: PostgreSQL 16
- **Autenticación**: JWT (JSON Web Tokens)
- **Contenedores**: Docker & Docker Compose
- **Testing**: JUnit, Postman Collections
- **Build Tool**: Maven

## 📦 Estructura del Proyecto

```
src/main/java/com/acme/vocatio/
├── config/           # Configuración de seguridad y JWT
├── controller/       # Controladores REST
├── dto/             # DTOs para requests/responses
├── exception/       # Excepciones personalizadas
├── model/           # Entidades JPA
├── repository/      # Repositorios de datos
├── security/        # Servicios de seguridad
├── service/         # Lógica de negocio
└── validation/      # Validadores personalizados

BD/                  # Documentación y datos de ejemplo
postman/            # Colecciones de pruebas
```

## 🚀 Guía de Inicio Rápido

### 1. Prerrequisitos
- Java 21+
- Docker & Docker Compose
- Maven (incluido con el wrapper)

### 2. Clonar y Configurar
```bash
git clone [repo-url]
cd Vocatio-backend
```

### 3. Iniciar la Base de Datos
```bash
# Iniciar PostgreSQL con Docker
docker-compose up -d

# Verificar que esté funcionando
docker-compose ps
```

### 4. Ejecutar la Aplicación
```bash
# Usando el wrapper de Maven
./mvnw spring-boot:run

# O en Windows
.\mvnw.cmd spring-boot:run
```

### 5. Verificar Funcionamiento
- **API Base**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health (si está habilitado)
- **Base de Datos**: localhost:5434

## 👥 **Guía para Colaboradores - Configuración del Entorno**

### 📋 **Pasos para Nuevos Desarrolladores**

Si eres un compañero de equipo que va a trabajar en este proyecto, sigue estos pasos **exactamente en este orden**:

#### **Paso 1: Verificar Prerrequisitos**
```bash
# Verificar Java 21+
java -version

# Verificar Docker
docker --version
docker-compose --version

# Si no tienes Docker, descárgalo de: https://www.docker.com/products/docker-desktop
```

#### **Paso 2: Clonar el Repositorio**
```bash
# Clona el proyecto
git clone [URL-DEL-REPOSITORIO]
cd Vocatio-backend

# Verifica que tienes todos los archivos
ls -la  # En Linux/Mac
dir     # En Windows
```

#### **Paso 3: Configurar la Base de Datos (CRÍTICO)**
```bash
# 1. Iniciar el contenedor de PostgreSQL
docker-compose up -d

# 2. Verificar que PostgreSQL esté corriendo
docker-compose ps
# Debes ver: postgres_vocatio   Up   5434->5432/tcp

# 3. Verificar la conexión a la BD
docker-compose exec postgres psql -U postgres -d vocatio_db -c "SELECT version();"
```

#### **Paso 4: Primera Ejecución de la Aplicación**
```bash
# 1. Compilar el proyecto (esto puede tomar unos minutos la primera vez)
./mvnw clean compile

# 2. Ejecutar la aplicación
./mvnw spring-boot:run

# En Windows, usar:
# .\mvnw.cmd clean compile
# .\mvnw.cmd spring-boot:run
```

#### **Paso 5: Verificar que Todo Funciona**
```bash
# Verificar que la app esté corriendo en puerto 8080
netstat -an | grep :8080     # Linux/Mac
netstat -an | findstr :8080  # Windows

# Probar el endpoint básico
curl http://localhost:8080/auth/register -X POST -H "Content-Type: application/json" -d '{"email":"test@test.com","password":"Password1","rememberMe":false}'
```

#### **Paso 6: Configurar Postman (Opcional pero Recomendado)**
```bash
# 1. Abrir Postman
# 2. Importar archivos del proyecto:
#    - postman/Vocatio-M1.postman_collection.json
#    - postman/Vocatio-local.postman_environment.json
# 3. Seleccionar environment "Vocatio - Local"
# 4. Ejecutar la colección completa para verificar
```

### 🚨 **Problemas Comunes y Soluciones**

#### **Error: Puerto 8080 ya está en uso**
```bash
# Encontrar qué proceso usa el puerto
netstat -tulpn | grep :8080  # Linux
netstat -ano | findstr :8080 # Windows

# Matar el proceso o cambiar el puerto en application.properties
# server.port=8081
```

#### **Error: No se puede conectar a PostgreSQL**
```bash
# Verificar que Docker esté corriendo
docker ps

# Reiniciar los contenedores
docker-compose down
docker-compose up -d

# Verificar logs de PostgreSQL
docker-compose logs postgres
```

#### **Error: Tablas no se crean automáticamente**
```bash
# Verificar la configuración en application.properties
# Debe tener: spring.jpa.hibernate.ddl-auto=update

# Si persiste, conectarse a la BD y verificar:
docker-compose exec postgres psql -U postgres -d vocatio_db
\dt  # Listar tablas
\q   # Salir
```

#### **Error de compilación Maven**
```bash
# Limpiar y recompilar
./mvnw clean
./mvnw compile

# Si persiste, verificar versión de Java
java -version  # Debe ser 21+
```

### 🔄 **Flujo de Trabajo Diario**

#### **Al Iniciar el Día:**
```bash
# 1. Actualizar código
git pull origin main

# 2. Iniciar servicios
docker-compose up -d

# 3. Ejecutar aplicación
./mvnw spring-boot:run
```

#### **Al Terminar el Día:**
```bash
# 1. Parar la aplicación (Ctrl+C)

# 2. Parar servicios Docker (opcional)
docker-compose down
```

### 📊 **Verificar Estado del Sistema**

#### **Comandos de Verificación:**
```bash
# Estado de contenedores
docker-compose ps

# Logs de la aplicación Spring Boot
# (Se muestran en la consola donde ejecutaste mvnw spring-boot:run)

# Logs de PostgreSQL
docker-compose logs postgres

# Verificar conexión a la BD
docker-compose exec postgres psql -U postgres -d vocatio_db -c "SELECT COUNT(*) FROM users;"
```

### 🆘 **Si Nada Funciona**

#### **Reset Completo:**
```bash
# 1. Parar todo
docker-compose down

# 2. Limpiar volúmenes (CUIDADO: Esto borra los datos)
docker-compose down -v

# 3. Limpiar proyecto Maven
./mvnw clean

# 4. Empezar desde cero
docker-compose up -d
./mvnw clean compile
./mvnw spring-boot:run
```

### 📞 **Contacto para Soporte**

Si después de seguir estos pasos tienes problemas:

1. **Verifica los logs** de Docker y Spring Boot
2. **Comparte el mensaje de error completo**
3. **Indica qué paso específico falló**

### 🎯 **Confirmación de Configuración Exitosa**

✅ **Sabrás que todo está funcionando cuando:**
- Docker muestre: `postgres_vocatio   Up`
- Spring Boot inicie sin errores
- Puedas registrar un usuario via Postman
- La aplicación responda en `http://localhost:8080`

## 🧪 Testing con Postman

### Importar Colección
1. Abre Postman
2. Importa `postman/Vocatio-M1.postman_collection.json`
3. Importa `postman/Vocatio-local.postman_environment.json`
4. Selecciona el environment "Vocatio - Local"

### Ejecutar Pruebas
La colección incluye pruebas automáticas para:
- ✅ Registro de usuarios
- ✅ Login con credenciales válidas/inválidas
- ✅ Gestión de perfiles
- ✅ Validaciones de edad, grado académico e intereses
- ✅ Manejo de tokens JWT

## 📊 API Endpoints

### Autenticación
```http
POST /auth/register    # Registrar nuevo usuario
POST /auth/login       # Iniciar sesión
```

### Perfil de Usuario
```http
GET  /users/me         # Obtener perfil actual
PUT  /users/me         # Actualizar perfil
```

### Ejemplo de Registro
```json
POST /auth/register
{
  "email": "usuario@vocatio.test",
  "password": "Password1",
  "rememberMe": false
}
```

### Ejemplo de Actualización de Perfil
```json
PUT /users/me
{
  "age": 18,
  "grade": "UNIVERSIDAD_1",
  "interests": ["Tecnología", "Ciencias", "Arte"]
}
```

## 🔒 Seguridad

### Políticas de Contraseña
- Mínimo 8 caracteres
- Al menos una letra mayúscula
- Al menos un número

### Tokens JWT
- **Access Token**: 15 minutos de duración
- **Refresh Token**: 7 días (168 horas)
- **Remember Me**: 30 días de duración extendida

### Validaciones de Perfil
- **Edad**: Entre 13 y 30 años
- **Grado académico**: Valores predefinidos válidos
- **Intereses**: Mínimo 1 interés requerido

## 🗄️ Base de Datos

### Configuración
- **Motor**: PostgreSQL 16
- **Puerto**: 5434 (para evitar conflictos)
- **Base**: vocatio_db
- **Usuario**: postgres / password

### Tablas Principales
- `users` - Usuarios del sistema
- `profiles` - Perfiles detallados
- `refresh_tokens` - Tokens de autenticación
- `carrera` - Catálogo de carreras
- `usuario_favoritos` - Carreras favoritas

Ver documentación completa en `BD/README.md`

## 🛠️ Comandos Útiles

```bash
# Compilar el proyecto
./mvnw compile

# Ejecutar tests
./mvnw test

# Limpiar y compilar
./mvnw clean install

# Ver logs de la base de datos
docker-compose logs postgres

# Conectar a PostgreSQL
docker-compose exec postgres psql -U postgres -d vocatio_db
```

## 📈 Estado del Desarrollo

### ✅ Completado (Módulo 1)
- Sistema de autenticación completo
- Gestión de perfiles básicos
- Validaciones robustas
- API REST funcional
- Tests automatizados

### 🚧 En Desarrollo
- Sistema de test vocacional
- Catálogo completo de carreras
- Sistema de favoritos
- Recomendaciones personalizadas

## 🐳 Docker

### Servicios Incluidos
```yaml
services:
  postgres:
    image: postgres:16
    ports:
      - "5434:5432"
    environment:
      POSTGRES_DB: vocatio_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
```

### Comandos Docker
```bash
# Iniciar servicios
docker-compose up -d

# Parar servicios
docker-compose down

# Ver estado
docker-compose ps

# Logs
docker-compose logs -f
```

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

Para soporte técnico o preguntas:
- Revisa la documentación en `BD/README.md`
- Ejecuta las pruebas de Postman
- Verifica los logs de la aplicación

---

**¡Vocatio - Descubre tu futuro profesional!** 🌟
