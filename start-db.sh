#!/bin/bash
set -euo pipefail

CONTAINER_NAME="vocatio_postgres"
DB_NAME="vocatio_db"
DB_USER="admin"
DB_PASSWORD="admin123"
PORT="5432"
IMAGE="postgres:15"
MAX_RETRIES=30
SLEEP_SECONDS=2

echo "Iniciando base de datos PostgreSQL para Vocatio..."

# Traer imagen
docker pull "${IMAGE}"

# Verificar si el contenedor ya existe (coincidencia exacta)
if [ -n "$(docker ps -a -q -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "El contenedor ya existe. Eliminándolo para recrearlo..."
    docker rm -f "${CONTAINER_NAME}"
fi

# Levantar nuevo contenedor
docker run -d \
  --name "${CONTAINER_NAME}" \
  -e POSTGRES_DB="${DB_NAME}" \
  -e POSTGRES_USER="${DB_USER}" \
  -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
  -p "${PORT}:5432" \
  -v "${CONTAINER_NAME}-data:/var/lib/postgresql/data" \
  "${IMAGE}"

echo "PostgreSQL iniciado (contenedor: ${CONTAINER_NAME}) en puerto ${PORT}"
echo "Base de datos: ${DB_NAME}  | Usuario: ${DB_USER} | Password: ${DB_PASSWORD}"
echo "Esperando a que PostgreSQL esté listo..."

# Esperar hasta que esté disponible con timeout
retries=0
until docker exec "${CONTAINER_NAME}" pg_isready -U "${DB_USER}" > /dev/null 2>&1; do
  retries=$((retries+1))
  if [ "${retries}" -ge "${MAX_RETRIES}" ]; then
    echo "Error: PostgreSQL no respondió después de $((MAX_RETRIES * SLEEP_SECONDS)) segundos."
    exit 1
  fi
  sleep "${SLEEP_SECONDS}"
  echo "Esperando... (${retries}/${MAX_RETRIES})"
done

echo "PostgreSQL está disponible."

# Crear el tipo enum si no existe.
# Si tienes un archivo SQL en ./sql/create_enum.sql se usará; si no, se ejecuta un DO block idempotente.
if [ -f "./sql/create_enum.sql" ]; then
  echo "Aplicando ./sql/create_enum.sql..."
  docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" < ./sql/create_enum.sql
else
  echo "Creando tipo tipo_recurso_enum (si no existe)..."
  docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_recurso_enum') THEN
    CREATE TYPE tipo_recurso_enum AS ENUM ('pdf','video','enlace','testimonio');
  END IF;
END
$$;
SQL
fi

echo "Tipo enum asegurado. Puedes iniciar el backend Spring Boot."
