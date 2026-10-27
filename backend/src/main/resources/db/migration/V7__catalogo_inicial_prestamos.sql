-- Inventario inicial visible en el catálogo de préstamos.
-- No reemplaza bienes existentes; solo agrega los que aún no estén registrados.
INSERT INTO catalogo_items (id, titulo, tipo, activo, fecha_alta, isbn, autor, editorial, anio_publicacion, marca, modelo, descripcion) VALUES
('00000000-0000-0000-0000-000000000110', 'Gafas de realidad virtual', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Meta', 'Quest 2', 'Visor inmersivo para actividades académicas y demostraciones.'),
('00000000-0000-0000-0000-000000000111', 'Cámara web HD', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Logitech', 'C920', 'Cámara web para clases, reuniones y grabaciones.'),
('00000000-0000-0000-0000-000000000112', 'Micrófono USB', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Audio-Technica', 'ATR2100x', 'Micrófono para clases, entrevistas y producciones.'),
('00000000-0000-0000-0000-000000000113', 'Pulsera tecnológica', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Xiaomi', 'Smart Band', 'Dispositivo wearable para actividades y mediciones.'),
('00000000-0000-0000-0000-000000000114', 'Laptop universitaria', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Lenovo', 'ThinkPad E14', 'Equipo portátil para trabajo académico.'),
('00000000-0000-0000-0000-000000000115', 'Teclado USB', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Logitech', 'K120', 'Teclado periférico para préstamo en laboratorio.'),
('00000000-0000-0000-0000-000000000116', 'Mouse USB', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Logitech', 'M100', 'Mouse óptico para computadoras.'),
('00000000-0000-0000-0000-000000000117', 'Adaptador HDMI', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'UGREEN', 'HDMI-USB-C', 'Adaptador para conectar equipos a pantallas.'),
('00000000-0000-0000-0000-000000000118', 'Memoria USB 64 GB', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Kingston', 'DataTraveler', 'Unidad USB para actividades académicas.'),
('00000000-0000-0000-0000-000000000119', 'Cargador universal', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Anker', 'PowerPort', 'Cargador para equipos y dispositivos compatibles.'),
('00000000-0000-0000-0000-000000000120', 'Puntero láser', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Logitech', 'R500', 'Puntero para exposiciones y presentaciones.'),
('00000000-0000-0000-0000-000000000121', 'Proyector multimedia', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Epson', 'PowerLite', 'Proyector para clases, eventos y presentaciones.'),
('00000000-0000-0000-0000-000000000122', 'Trípode para cámara', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Manfrotto', 'Compact', 'Trípode ajustable para cámaras y grabaciones.'),
('00000000-0000-0000-0000-000000000123', 'Auriculares', 'EQUIPO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, 'Sony', 'WH-CH520', 'Auriculares para clases y producción audiovisual.'),
('00000000-0000-0000-0000-000000000124', 'Clean Architecture', 'LIBRO', TRUE, CURRENT_TIMESTAMP, '9780134494166', 'Robert C. Martin', 'Prentice Hall', 2017, NULL, NULL, 'Buenas prácticas para desarrollo de software.'),
('00000000-0000-0000-0000-000000000125', 'El principito', 'LIBRO', TRUE, CURRENT_TIMESTAMP, '9780156012195', 'Antoine de Saint-Exupéry', 'Harcourt', 1943, NULL, NULL, 'Obra literaria para lectura y préstamo.'),
('00000000-0000-0000-0000-000000000126', 'Introducción a la psicología', 'LIBRO', TRUE, CURRENT_TIMESTAMP, '9786071505312', 'Charles G. Morris', 'Pearson', 2014, NULL, NULL, 'Texto introductorio para estudiantes.'),
('00000000-0000-0000-0000-000000000127', 'Mesa de tenis de mesa', 'OTRO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, NULL, NULL, 'Mesa y accesorios para actividades deportivas.'),
('00000000-0000-0000-0000-000000000128', 'Pelota de vóley', 'OTRO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, NULL, NULL, 'Pelota reglamentaria para práctica deportiva.'),
('00000000-0000-0000-0000-000000000129', 'Pelota de fútbol', 'OTRO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, NULL, NULL, 'Pelota para entrenamiento y actividades deportivas.'),
('00000000-0000-0000-0000-000000000130', 'Pelota de básquet', 'OTRO', TRUE, CURRENT_TIMESTAMP, NULL, NULL, NULL, NULL, NULL, NULL, 'Pelota para entrenamiento y actividades deportivas.')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ejemplares (id, catalogo_item_id, barcode, sala, estante, estado_fisico, notas, fecha_alta) VALUES
('00000000-0000-0000-0000-000000000210', '00000000-0000-0000-0000-000000000110', 'VR-001', 'Sala tecnológica', 'Gabinete T-1', 'DISPONIBLE', 'Equipo demo', CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000211', '00000000-0000-0000-0000-000000000111', 'CAM-001', 'Sala tecnológica', 'Gabinete T-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000212', '00000000-0000-0000-0000-000000000112', 'MIC-001', 'Sala tecnológica', 'Gabinete T-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000213', '00000000-0000-0000-0000-000000000113', 'PUL-001', 'Sala tecnológica', 'Gabinete T-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000214', '00000000-0000-0000-0000-000000000114', 'LAP-002', 'Laboratorio', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000215', '00000000-0000-0000-0000-000000000115', 'TEC-001', 'Laboratorio', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000216', '00000000-0000-0000-0000-000000000116', 'MOU-001', 'Laboratorio', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000217', '00000000-0000-0000-0000-000000000117', 'HDMI-001', 'Sala tecnológica', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000218', '00000000-0000-0000-0000-000000000118', 'USB-001', 'Sala tecnológica', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000219', '00000000-0000-0000-0000-000000000119', 'CAR-001', 'Sala tecnológica', 'Gabinete T-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000220', '00000000-0000-0000-0000-000000000120', 'PUN-001', 'Sala general', 'Gabinete A-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000221', '00000000-0000-0000-0000-000000000121', 'PRO-001', 'Sala audiovisual', 'Gabinete AV-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000222', '00000000-0000-0000-0000-000000000122', 'TRI-001', 'Sala audiovisual', 'Gabinete AV-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000223', '00000000-0000-0000-0000-000000000123', 'AUR-001', 'Sala audiovisual', 'Gabinete AV-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000224', '00000000-0000-0000-0000-000000000124', 'LIB-003', 'Sala general', 'Estante A-2', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000225', '00000000-0000-0000-0000-000000000125', 'LIB-004', 'Sala general', 'Estante A-3', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000226', '00000000-0000-0000-0000-000000000126', 'LIB-005', 'Sala general', 'Estante A-3', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000227', '00000000-0000-0000-0000-000000000127', 'DEP-001', 'Zona deportiva', 'Almacén D-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000228', '00000000-0000-0000-0000-000000000128', 'DEP-002', 'Zona deportiva', 'Almacén D-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000229', '00000000-0000-0000-0000-000000000129', 'DEP-003', 'Zona deportiva', 'Almacén D-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP),
('00000000-0000-0000-0000-000000000230', '00000000-0000-0000-0000-000000000130', 'DEP-004', 'Zona deportiva', 'Almacén D-1', 'DISPONIBLE', NULL, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
