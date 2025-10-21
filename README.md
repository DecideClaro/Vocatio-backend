# Vocatio Backend

Este repositorio contiene el backend de Vocatio desarrollado con Spring Boot. A continuación se listan los comandos más importantes y las verificaciones útiles para despliegues en Render.

## Requisitos

* Java 21
* Maven Wrapper (`./mvnw`)
* Base de datos PostgreSQL accesible mediante las variables de entorno `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.
* Variables de entorno para JWT: `JWT_SECRET`.

## Configuración del puerto y contexto

La aplicación expone un único puerto HTTP. Por defecto usa el puerto `8080`, pero si Render define la variable de entorno `PORT` la aplicación la respeta automáticamente.

El `context-path` (prefijo de todos los endpoints) está fijado en `/api/v1`. Si tu proveedor requiere exponer la API directamente en la raíz (`/`), puedes sobrescribirlo definiendo la variable de entorno `SERVER_CONTEXT_PATH`:

```bash
# Ejemplo para dejar la API en la raíz
SERVER_CONTEXT_PATH=""  # cadena vacía
```

> Nota: Render permite dejar el valor vacío utilizando un espacio en blanco o comillas en la configuración de variables de entorno.

## Endpoints públicos útiles

* `GET /api/v1/` – mensaje de bienvenida y acceso a Swagger UI.
* `GET /api/v1/actuator/health` – healthcheck expuesto para Render.
* `POST /api/v1/auth/login` y `POST /api/v1/auth/register` – autenticación.

Puedes verificar rápidamente que el despliegue está respondiendo con:

```bash
curl -i https://vocatio-backend.onrender.com/api/v1/actuator/health
```

Una respuesta `200 OK` con `{"status":"UP"}` confirma que la instancia está saludable y conectada al contenedor de la aplicación.

## Construcción local

```bash
./mvnw clean package
```

Para ejecutar la aplicación localmente (requiere que las variables de entorno estén definidas):

```bash
./mvnw spring-boot:run
```

## Docker

El repositorio incluye un `Dockerfile` multi-stage listo para Render. También puedes levantar los servicios definidos en `docker-compose.yml` para un entorno local con PostgreSQL.

## Solución de problemas

1. **404 en la URL raíz (`/`)** – recuerda que la API está montada bajo `/api/v1`. Ajusta el `context-path` con `SERVER_CONTEXT_PATH` si necesitas responder en `/`.
2. **Errores de conexión a base de datos** – comprueba que las variables de entorno de la base de datos estén configuradas en Render y que la instancia de PostgreSQL acepte conexiones externas.
3. **Errores de autenticación (401)** – asegúrate de enviar el token JWT emitido en `/auth/login` al consumir endpoints protegidos.

Si tras estas verificaciones el despliegue sigue fallando, revisa los logs de Render para identificar excepciones específicas.
