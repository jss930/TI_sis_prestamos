-- ============================================================================
-- Migración de datos iniciales para desarrollo y pruebas
-- ============================================================================

-- ================ USUARIOS ====================

INSERT INTO persona (nombres, apellidos, contacto_correo, contacto_telefono, estado) VALUES
('Alan', 'Turing', 'alan.turing@unsa.edu.pe', '+51987654321', 'ACTIVA'),
('Ada', 'Lovelace', 'ada.lovelace@unsa.edu.pe', '+51987654322', 'ACTIVA'),
('Donald', 'Knuth', 'dknuth@unsa.edu.pe', '+51987654323', 'ACTIVA'),
('Grace', 'Hopper', 'ghopper@unsa.edu.pe', '+51987654324', 'ACTIVA'),
('Linus', 'Torvalds', 'ltorvalds@unsa.edu.pe', '+51987654325', 'ACTIVA');

INSERT INTO "admin" (id_admin, permisos) VALUES
(1, '["ALL_PRIVILEGES"]'::jsonb),
(4, '["READ_CATALOG", "UPDATE_INVENTORY"]'::jsonb),
(5, '["MANAGE_LOANS", "MANAGE_SANCTIONS"]'::jsonb),
(2, '["READ_CATALOG"]'::jsonb),
(3, '["READ_CATALOG"]'::jsonb);

INSERT INTO perfil (id_perfil, prestamo_domicilio_equipos, plazo_extendido_libros, cantidad_maxima_simultanea) VALUES
(1, true, true, 5),
(2, false, false, 3),
(3, true, true, 10),
(4, true, true, 5),
(5, true, false, 4);

INSERT INTO alumno (id_alumno, cui) VALUES
(1, '20230001'),
(2, '20230002'),
(5, '20230005');

INSERT INTO docente (id_docente, departamento, categoria) VALUES
(3, 'Ciencia de la Computacion', 'Principal DED'),
(4, 'Ciencia de la Computacion', 'Asociado TC'),
(1, 'Ciencia de la Computacion', 'Principal DE');

INSERT INTO administrativo (id_administrativo, area) VALUES
(4, 'Biblioteca EPCC'),
(5, 'Sistemas e Informatica'),
(3, 'Jefatura Academica');


-- ================= UBICACIONES =====================

INSERT INTO ubicacion_fisica (sala, estante) VALUES
('Sala de Lectura CC', 'Estante A-1'),
('Laboratorio VR', 'Armario B-2'),
('Almacen Deportivo', 'Caja D-3'),
('Sala de Investigacion', 'Estante C-4'),
('Hemeroteca', 'Estante H-1');


-- ================= ITEMS =====================

INSERT INTO item (titulo, categoria, cantidad_total, cantidad_disponible) VALUES
('Introduction to Algorithms 4th Ed', 'LIBRO', 5, 4),
('Clean Code', 'LIBRO', 3, 2),
('Meta Quest 3 (Oculus)', 'EQUIPO', 2, 1),
('Pelotas de Ping Pong (Set x6)', 'OTRO', 10, 9),
('Pelotas de Voley Molten', 'OTRO', 4, 3);

INSERT INTO libro (id_item, isbn, autor, editorial, edicion) VALUES
(1, '978-0262046305', 'Thomas H. Cormen', 'MIT Press', '4ta'),
(2, '978-0132350884', 'Robert C. Martin', 'Prentice Hall', '1ra');

INSERT INTO equipo (id_item, numero_serie, marca, modelo) VALUES
(3, 'MQ3-10029384-VR', 'Meta', 'Quest 3');


-- ================= EJEMPLARES =====================

INSERT INTO ejemplar (id_item, id_ubicacion, codigo_inventario, estado_fisico, disponible) VALUES
(1, 1, 'INV-LIB-001', 'DISPONIBLE', true),
(1, 1, 'INV-LIB-002', 'DISPONIBLE', false),
(2, 1, 'INV-LIB-003', 'DISPONIBLE', true),
(3, 2, 'INV-EQP-001', 'DISPONIBLE', false),
(4, 3, 'INV-OTH-001', 'DISPONIBLE', true);


-- ================= PRÉSTAMOS =====================

INSERT INTO prestamo (id_persona, id_ejemplar, fecha_inicio, fecha_vencimiento, fecha_devolucion_real, lugar_uso, estado) VALUES
(2, 2, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '2 days', NULL, 'DOMICILIO', 'ACTIVO'),
(1, 4, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '3 days', NULL, 'EN_CAMPUS', 'VENCIDO'),
(3, 1, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '8 days', CURRENT_TIMESTAMP - INTERVAL '8 days', 'DOMICILIO', 'DEVUELTO'),
(4, 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, 'DOMICILIO', 'ACTIVO'),
(5, 5, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day', NULL, 'EN_CAMPUS', 'ACTIVO');


-- ================= RESERVAS =====================

INSERT INTO reserva (id_persona, id_item, id_ejemplar, fecha_reserva, fecha_expiracion, estado) VALUES
(1, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', 'PENDIENTE'),
(2, 3, 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'EXPIRADA'),
(3, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '2 days', 'CONFIRMADA'),
(4, 4, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', 'PENDIENTE'),
(5, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 day', 'PENDIENTE');


-- ================= POLÍTICAS =====================

INSERT INTO politica_prestamo (tipo_persona, tipo_item, duracion_maxima_dias, cantidad_maxima_simultanea, permite_prestamo_domicilio) VALUES
('ALUMNO', 'LIBRO', 7, 3, true),
('ALUMNO', 'EQUIPO', 1, 1, false),
('DOCENTE', 'LIBRO', 30, 10, true),
('DOCENTE', 'EQUIPO', 7, 2, true),
('ADMINISTRATIVO', 'OTRO', 3, 2, true);


-- ================= SANCIONES =====================

INSERT INTO sancion (id_persona, id_prestamo, motivo, fecha_inicio, fecha_fin, estado) VALUES
(1, 2, 'DEVOLUCION_TARDIA', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP + INTERVAL '4 days', 'ACTIVA'),
(3, 3, 'DANO', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '15 days', 'CUMPLIDA'),
(2, 1, 'DEVOLUCION_TARDIA', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'CUMPLIDA'),
(5, 5, 'PERDIDA', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '28 days', 'ACTIVA'),
(4, 4, 'DANO', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '6 days', 'ACTIVA');


-- ================= HISTORIAL =====================

INSERT INTO historial_prestamo (id_persona, id_prestamo, tipo_evento, fecha_evento) VALUES
(2, 1, 'CREADO', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(1, 2, 'CREADO', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(1, 2, 'VENCIDO', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(1, 2, 'SANCIONADO', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(3, 3, 'DEVUELTO', CURRENT_TIMESTAMP - INTERVAL '8 days');