-- Dos cargos por area, sin reescribir el historial anterior.
CREATE TABLE control_responsables (id INTEGER PRIMARY KEY);
INSERT INTO control_responsables(id) VALUES (1);
CREATE TABLE cargos_responsables (
    usuario_id UUID NOT NULL REFERENCES responsabilidades_usuarios(usuario_id),
    categoria VARCHAR(30) NOT NULL,
    cargo VARCHAR(20) NOT NULL,
    PRIMARY KEY (usuario_id, categoria),
    CONSTRAINT uq_cargo_por_area UNIQUE (categoria, cargo),
    CONSTRAINT ck_cargo_responsable CHECK (cargo IN ('PRINCIPAL', 'SUPLENTE')),
    CONSTRAINT ck_area_responsable CHECK (categoria IN ('BIBLIOTECA','DEPORTES','TECNOLOGIA','COMPUTACION'))
);
ALTER TABLE historial_responsables ADD COLUMN cargo VARCHAR(20);
INSERT INTO cargos_responsables(usuario_id, categoria, cargo)
SELECT usuario_id, 'BIBLIOTECA', CASE WHEN puesto = 1 THEN 'PRINCIPAL' WHEN puesto = 2 THEN 'SUPLENTE' ELSE 'REVISAR' END
FROM (
    SELECT r.usuario_id, ROW_NUMBER() OVER (ORDER BY
        COALESCE((SELECT MIN(h.fecha) FROM historial_responsables h
            WHERE h.usuario_id = r.usuario_id AND h.categoria = 'BIBLIOTECA' AND h.accion = 'ASIGNADO'), u.fecha_alta), r.usuario_id) AS puesto
    FROM responsabilidades_usuarios r JOIN usuarios u ON u.id = r.usuario_id
    WHERE CONCAT(',', r.categorias, ',') LIKE '%,BIBLIOTECA,%'
) asignaciones;
INSERT INTO cargos_responsables(usuario_id, categoria, cargo)
SELECT usuario_id, 'DEPORTES', CASE WHEN puesto = 1 THEN 'PRINCIPAL' WHEN puesto = 2 THEN 'SUPLENTE' ELSE 'REVISAR' END
FROM (
    SELECT r.usuario_id, ROW_NUMBER() OVER (ORDER BY
        COALESCE((SELECT MIN(h.fecha) FROM historial_responsables h
            WHERE h.usuario_id = r.usuario_id AND h.categoria = 'DEPORTES' AND h.accion = 'ASIGNADO'), u.fecha_alta), r.usuario_id) AS puesto
    FROM responsabilidades_usuarios r JOIN usuarios u ON u.id = r.usuario_id
    WHERE CONCAT(',', r.categorias, ',') LIKE '%,DEPORTES,%'
) asignaciones;
INSERT INTO cargos_responsables(usuario_id, categoria, cargo)
SELECT usuario_id, 'TECNOLOGIA', CASE WHEN puesto = 1 THEN 'PRINCIPAL' WHEN puesto = 2 THEN 'SUPLENTE' ELSE 'REVISAR' END
FROM (
    SELECT r.usuario_id, ROW_NUMBER() OVER (ORDER BY
        COALESCE((SELECT MIN(h.fecha) FROM historial_responsables h
            WHERE h.usuario_id = r.usuario_id AND h.categoria = 'TECNOLOGIA' AND h.accion = 'ASIGNADO'), u.fecha_alta), r.usuario_id) AS puesto
    FROM responsabilidades_usuarios r JOIN usuarios u ON u.id = r.usuario_id
    WHERE CONCAT(',', r.categorias, ',') LIKE '%,TECNOLOGIA,%'
) asignaciones;
INSERT INTO cargos_responsables(usuario_id, categoria, cargo)
SELECT usuario_id, 'COMPUTACION', CASE WHEN puesto = 1 THEN 'PRINCIPAL' WHEN puesto = 2 THEN 'SUPLENTE' ELSE 'REVISAR' END
FROM (
    SELECT r.usuario_id, ROW_NUMBER() OVER (ORDER BY
        COALESCE((SELECT MIN(h.fecha) FROM historial_responsables h
            WHERE h.usuario_id = r.usuario_id AND h.categoria = 'COMPUTACION' AND h.accion = 'ASIGNADO'), u.fecha_alta), r.usuario_id) AS puesto
    FROM responsabilidades_usuarios r JOIN usuarios u ON u.id = r.usuario_id
    WHERE CONCAT(',', r.categorias, ',') LIKE '%,COMPUTACION,%'
) asignaciones;
