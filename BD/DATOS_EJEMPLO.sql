# Datos de ejemplo para testing post-migración
# Ejecutar estos inserts después de la migración exitosa

-- Insertar datos de prueba para Carreras
INSERT INTO Carrera (nombre, descripcion_corta, descripcion_larga, plan_de_estudios, salida_laboral) VALUES 
('Ingeniería de Sistemas', 
 'Desarrollo de software y tecnología', 
 'Carrera enfocada en el desarrollo de aplicaciones, sistemas de información y tecnologías emergentes.',
 'Programación, Base de Datos, Redes, Desarrollo Web, Inteligencia Artificial',
 'Desarrollador de Software, Arquitecto de Sistemas, Consultor IT, Gerente de Proyectos Tecnológicos'),

('Medicina', 
 'Ciencias de la salud y medicina clínica', 
 'Formación integral en ciencias médicas para la promoción, prevención y tratamiento de la salud.',
 'Anatomía, Fisiología, Patología, Farmacología, Medicina Interna, Cirugía',
 'Médico General, Especialista Médico, Investigador Médico, Médico de Emergencias'),

('Derecho', 
 'Ciencias jurídicas y sistema legal', 
 'Estudio del sistema legal, normativo y la aplicación de la justicia en la sociedad.',
 'Derecho Civil, Penal, Constitucional, Laboral, Internacional, Procedimiento',
 'Abogado Litigante, Consultor Jurídico, Juez, Fiscal, Notario'),

('Psicología', 
 'Ciencias del comportamiento humano', 
 'Estudio científico del comportamiento y los procesos mentales del ser humano.',
 'Psicología General, Clínica, Social, Educativa, Organizacional, Neuropsicología',
 'Psicólogo Clínico, Psicólogo Educativo, Consultor Organizacional, Investigador');

-- Insertar Universidades de ejemplo
INSERT INTO Universidad (nombre_universidad, logo_url, sitio_web) VALUES 
('Universidad Nacional de Colombia', 'https://unal.edu.co/logo.png', 'https://unal.edu.co'),
('Universidad de los Andes', 'https://uniandes.edu.co/logo.png', 'https://uniandes.edu.co'),
('Pontificia Universidad Javeriana', 'https://javeriana.edu.co/logo.png', 'https://javeriana.edu.co'),
('Universidad del Rosario', 'https://urosario.edu.co/logo.png', 'https://urosario.edu.co');

-- Insertar Áreas de Interés
INSERT INTO AreaInteres (nombre_area) VALUES 
('Tecnología e Informática'),
('Ciencias de la Salud'),
('Ciencias Jurídicas'),
('Ciencias Sociales y Humanas'),
('Ingeniería'),
('Arte y Diseño'),
('Ciencias Económicas'),
('Educación');

-- Insertar Duraciones
INSERT INTO Duracion (rango_duracion) VALUES 
('2-3 años'),
('4-5 años'),
('6-7 años'),
('8+ años');

-- Insertar Modalidades
INSERT INTO Modalidad (nombre_modalidad) VALUES 
('Presencial'),
('Virtual'),
('Semipresencial'),
('A distancia');

-- Insertar Test Vocacional básico
INSERT INTO TestVocacional (nombre_test, descripcion, version_test) VALUES 
('Test de Orientación Vocacional Básico', 
 'Evaluación de intereses y habilidades para orientación profesional', 
 '1.0');

-- Insertar algunos logros básicos
INSERT INTO Logro (nombre, descripcion, icono) VALUES 
('Primer Test Completado', 'Completaste tu primer test vocacional', 'trophy'),
('Explorador de Carreras', 'Agregaste 5 carreras a favoritos', 'star'),
('Usuario Activo', 'Has estado activo por 7 días consecutivos', 'calendar'),
('Buscador Curioso', 'Realizaste 10 búsquedas de carreras', 'search');

-- Relaciones de ejemplo: Carreras con Áreas
INSERT INTO CarreraArea (id_carrera, id_area) VALUES 
(1, 1), -- Ing. Sistemas con Tecnología
(2, 2), -- Medicina con Ciencias de la Salud
(3, 3), -- Derecho con Ciencias Jurídicas
(4, 4); -- Psicología con Ciencias Sociales

-- Relaciones de ejemplo: Carreras con Duraciones
INSERT INTO CarreraDuracion (id_carrera, id_duracion) VALUES 
(1, 2), -- Ing. Sistemas: 4-5 años
(2, 3), -- Medicina: 6-7 años
(3, 2), -- Derecho: 4-5 años
(4, 2); -- Psicología: 4-5 años

-- Relaciones de ejemplo: Carreras con Modalidades
INSERT INTO CarreraModalidad (id_carrera, id_modalidad) VALUES 
(1, 1), (1, 2), (1, 3), -- Ing. Sistemas: Todas las modalidades
(2, 1), -- Medicina: Solo presencial
(3, 1), (3, 3), -- Derecho: Presencial y semipresencial
(4, 1), (4, 2); -- Psicología: Presencial y virtual

-- Relaciones de ejemplo: Carreras con Universidades
INSERT INTO CarreraUniversidad (id_carrera, id_universidad) VALUES 
(1, 1), (1, 2), -- Ing. Sistemas en Nacional y Andes
(2, 1), (2, 3), -- Medicina en Nacional y Javeriana
(3, 2), (3, 4), -- Derecho en Andes y Rosario
(4, 3), (4, 4); -- Psicología en Javeriana y Rosario