-- Conserva las fechas históricas y añade precisión horaria para entregas nuevas.
ALTER TABLE prestamos ADD COLUMN fecha_hora_entrega TIMESTAMP WITH TIME ZONE;
ALTER TABLE prestamos ADD COLUMN fecha_hora_vencimiento TIMESTAMP WITH TIME ZONE;
ALTER TABLE prestamos ADD COLUMN fecha_hora_devolucion TIMESTAMP WITH TIME ZONE;

UPDATE prestamos
SET fecha_hora_entrega = fecha_inicio::timestamp AT TIME ZONE 'America/Lima',
    fecha_hora_vencimiento = (fecha_vencimiento::timestamp + TIME '20:00') AT TIME ZONE 'America/Lima',
    fecha_hora_devolucion = CASE WHEN fecha_devolucion IS NULL THEN NULL
        ELSE (fecha_devolucion::timestamp + TIME '12:00') AT TIME ZONE 'America/Lima' END;

ALTER TABLE prestamos ALTER COLUMN fecha_hora_entrega SET NOT NULL;
ALTER TABLE prestamos ALTER COLUMN fecha_hora_vencimiento SET NOT NULL;
CREATE INDEX idx_prestamos_horario ON prestamos (estado, fecha_hora_entrega, fecha_hora_vencimiento);
