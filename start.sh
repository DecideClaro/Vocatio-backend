#!/bin/bash

# Script de inicio para Render
echo "Starting Vocatio application on Render..."

# Configurar las variables de entorno por defecto si no existen
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
export JAVA_OPTS=${JAVA_OPTS:--Xmx512m -XX:+UseG1GC}

# Ejecutar la aplicación
exec java $JAVA_OPTS -jar target/vocatio-0.0.1-SNAPSHOT.jar