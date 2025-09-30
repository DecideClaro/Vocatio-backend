# Script de migración para Vocatio - Docker Compose
# Ejecutar desde PowerShell en la raíz del proyecto

Write-Host "🚀 Iniciando migración de Vocatio con Docker Compose" -ForegroundColor Green

# Verificar que Docker Compose está corriendo
Write-Host "📋 Verificando estado de Docker Compose..." -ForegroundColor Yellow
$dockerStatus = docker-compose ps --format json | ConvertFrom-Json
if ($dockerStatus.State -ne "running") {
    Write-Host "⚠️  Iniciando contenedores Docker..." -ForegroundColor Yellow
    docker-compose up -d
    Start-Sleep -Seconds 10
}

# Paso 1: Backup de seguridad
Write-Host "💾 Creando backup de seguridad..." -ForegroundColor Yellow
$backupDate = Get-Date -Format "yyyyMMdd_HHmmss"
$backupFile = "backup_vocatio_$backupDate.sql"

try {
    docker-compose exec -T postgres pg_dump -U postgres vocatio_db > $backupFile
    Write-Host "✅ Backup creado: $backupFile" -ForegroundColor Green
} catch {
    Write-Host "❌ Error creando backup: $_" -ForegroundColor Red
    exit 1
}

# Paso 2: Copiar scripts al contenedor
Write-Host "📁 Copiando scripts al contenedor..." -ForegroundColor Yellow
try {
    docker cp "BD/DDL_PG_MIGRATION.sql" postgres_vocatio:/tmp/
    docker cp "BD/VERIFICACION_MIGRACION.sql" postgres_vocatio:/tmp/
    if (Test-Path "BD/DATOS_EJEMPLO.sql") {
        docker cp "BD/DATOS_EJEMPLO.sql" postgres_vocatio:/tmp/
    }
    Write-Host "✅ Scripts copiados al contenedor" -ForegroundColor Green
} catch {
    Write-Host "❌ Error copiando scripts: $_" -ForegroundColor Red
    exit 1
}

# Paso 3: Ejecutar migración
Write-Host "🔄 Ejecutando migración..." -ForegroundColor Yellow
try {
    docker-compose exec postgres psql -U postgres -d vocatio_db -f /tmp/DDL_PG_MIGRATION.sql
    Write-Host "✅ Migración ejecutada exitosamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en la migración: $_" -ForegroundColor Red
    Write-Host "🔄 Puedes restaurar desde el backup: $backupFile" -ForegroundColor Yellow
    exit 1
}

# Paso 4: Verificar migración
Write-Host "🔍 Verificando migración..." -ForegroundColor Yellow
try {
    docker-compose exec postgres psql -U postgres -d vocatio_db -f /tmp/VERIFICACION_MIGRACION.sql
    Write-Host "✅ Verificación completada" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Error en verificación: $_" -ForegroundColor Yellow
}

# Paso 5: Insertar datos de ejemplo (opcional)
$insertData = Read-Host "¿Insertar datos de ejemplo? (y/N)"
if ($insertData -eq "y" -or $insertData -eq "Y") {
    Write-Host "📊 Insertando datos de ejemplo..." -ForegroundColor Yellow
    try {
        docker-compose exec postgres psql -U postgres -d vocatio_db -f /tmp/DATOS_EJEMPLO.sql
        Write-Host "✅ Datos de ejemplo insertados" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Error insertando datos de ejemplo: $_" -ForegroundColor Yellow
    }
}

# Paso 6: Verificar conexión desde la aplicación
Write-Host "🧪 Verificando conexión desde la aplicación..." -ForegroundColor Yellow
Write-Host "Por favor, ejecuta en otra terminal:" -ForegroundColor Cyan
Write-Host "./mvnw clean compile" -ForegroundColor White
Write-Host "./mvnw spring-boot:run" -ForegroundColor White

Write-Host ""
Write-Host "🎉 ¡Migración completada!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Resumen:" -ForegroundColor Yellow
Write-Host "  • Backup creado: $backupFile"
Write-Host "  • Nuevas tablas agregadas a la base de datos"
Write-Host "  • Funcionalidad existente preservada"
Write-Host "  • Nuevos endpoints disponibles en /api/carreras y /api/favoritos"
Write-Host ""
Write-Host "🧪 Próximos pasos:" -ForegroundColor Yellow
Write-Host "  1. Compila y ejecuta la aplicación Java"
Write-Host "  2. Prueba tus endpoints existentes de Postman"
Write-Host "  3. Prueba los nuevos endpoints de carreras"
Write-Host "  4. Si algo sale mal, restaura desde: $backupFile"
Write-Host ""

# Mostrar estado final de contenedores
Write-Host "🐳 Estado actual de contenedores:" -ForegroundColor Yellow
docker-compose ps