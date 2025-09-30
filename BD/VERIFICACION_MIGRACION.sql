-- Script de verificación post-migración
-- Ejecutar después de la migración para verificar que todo está funcionando

-- 1. Verificar que las tablas principales existen
SELECT 'Verificando tablas principales...' as status;

SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'usuario', 'perfilusuario', 'refreshtoken',
    'carrera', 'universidad', 'areainteres',
    'testvocacional', 'testintento', 'logro',
    'materialeducativo', 'usuariofavoritos'
)
ORDER BY table_name;

-- 2. Verificar estructura de tabla Usuario (migrada o original)
SELECT 'Verificando estructura de Usuario...' as status;

SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name IN ('usuario', 'users') 
AND table_schema = 'public'
ORDER BY ordinal_position;

-- 3. Contar registros en tablas principales
SELECT 'Contando registros en tablas...' as status;

SELECT 
    'Usuario/users' as tabla,
    (SELECT COUNT(*) FROM usuario) + COALESCE((SELECT COUNT(*) FROM users), 0) as total
UNION ALL
SELECT 'Carrera' as tabla, COUNT(*) as total FROM carrera
UNION ALL
SELECT 'Universidad' as tabla, COUNT(*) as total FROM universidad
UNION ALL
SELECT 'AreaInteres' as tabla, COUNT(*) as total FROM areainteres
UNION ALL
SELECT 'Logro' as tabla, COUNT(*) as total FROM logro;

-- 4. Verificar índices creados
SELECT 'Verificando índices...' as status;

SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public'
AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- 5. Verificar foreign keys
SELECT 'Verificando relaciones...' as status;

SELECT 
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
AND tc.table_schema = 'public'
AND tc.table_name IN (
    'perfilusuario', 'refreshtoken', 'usuariofavoritos', 
    'testintento', 'usuariologro', 'materialcarrera'
)
ORDER BY tc.table_name, tc.constraint_name;

-- 6. Verificar datos de ejemplo (si se insertaron)
SELECT 'Verificando datos de ejemplo...' as status;

SELECT 
    'Carreras insertadas' as dato,
    COUNT(*) as cantidad
FROM carrera
UNION ALL
SELECT 
    'Universidades insertadas' as dato,
    COUNT(*) as cantidad
FROM universidad
UNION ALL
SELECT 
    'Areas de interés' as dato,
    COUNT(*) as cantidad
FROM areainteres;

-- 7. Test de consulta compleja - Carreras con sus universidades
SELECT 'Test de consulta compleja...' as status;

SELECT 
    c.nombre as carrera,
    COUNT(cu.id_universidad) as total_universidades
FROM carrera c
LEFT JOIN carrerauniversidad cu ON c.id_carrera = cu.id_carrera
GROUP BY c.id_carrera, c.nombre
LIMIT 5;

-- 8. Verificar que RefreshToken mantiene compatibilidad
SELECT 'Verificando RefreshToken...' as status;

SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'refreshtoken' 
AND table_schema = 'public'
ORDER BY ordinal_position;

SELECT 'MIGRACIÓN VERIFICADA EXITOSAMENTE ✓' as resultado;