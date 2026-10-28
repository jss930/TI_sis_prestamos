-- El modelo operativo usa permisos directos por usuario y no clasifica bienes por áreas responsables.
DROP TABLE IF EXISTS cargos_responsables;
DROP TABLE IF EXISTS historial_responsables;
DROP TABLE IF EXISTS responsabilidades_usuarios;
DROP TABLE IF EXISTS control_responsables;

ALTER TABLE catalogo_items DROP COLUMN IF EXISTS area;

UPDATE usuarios
SET permisos = REPLACE(REPLACE(REPLACE(permisos,
    'GESTIONAR_RESERVAS,', ''), ',GESTIONAR_RESERVAS', ''), 'GESTIONAR_RESERVAS', '');

CREATE INDEX idx_reservas_copia_horario
    ON reservas (ejemplar_id, inicio_reserva, fin_reserva);

CREATE INDEX idx_prestamos_copia_horario
    ON prestamos (ejemplar_id, estado, fecha_hora_entrega, fecha_hora_vencimiento);

ALTER TABLE reservas ADD CONSTRAINT ck_reservas_intervalo
    CHECK (inicio_reserva IS NULL OR fin_reserva IS NULL OR fin_reserva > inicio_reserva);
