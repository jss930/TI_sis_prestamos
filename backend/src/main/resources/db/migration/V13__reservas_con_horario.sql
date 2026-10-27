-- Horario futuro solicitado por el usuario. Las filas históricas permanecen sin horario.
ALTER TABLE reservas ADD COLUMN inicio_reserva TIMESTAMP WITH TIME ZONE;
ALTER TABLE reservas ADD COLUMN fin_reserva TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_reservas_horario
    ON reservas (estado, inicio_reserva, fin_reserva);
