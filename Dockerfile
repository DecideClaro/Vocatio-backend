# Multi-stage build para optimizar el tamaño de imagen
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Crear usuario no-root por seguridad
RUN groupadd -r vocatio && useradd -r -g vocatio vocatio

# Copiar el JAR construido
COPY --from=build /app/target/vocatio-0.0.1-SNAPSHOT.jar app.jar

# Cambiar ownership del archivo
RUN chown vocatio:vocatio app.jar

# Cambiar a usuario no-root
USER vocatio

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -XX:+UseContainerSupport -XX:+UseG1GC"

# Puerto dinámico para Render
EXPOSE $PORT

# Comando para ejecutar la aplicación
ENTRYPOINT exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar