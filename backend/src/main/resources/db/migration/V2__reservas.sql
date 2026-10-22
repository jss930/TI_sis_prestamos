-- Contexto reservations: agregado Reserva.

CREATE TABLE reservas (
    id UUID PRIMARY KEY,
    prestatario_id UUID NOT NULL,
    catalogo_item_id UUID NOT NULL,
    operador_id UUID,
    prestamo_id UUID,
    estado VARCHAR(20) NOT NULL,
    motivo VARCHAR(500),
    fecha_alta TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_decision TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_reservas_prestatario
        FOREIGN KEY (prestatario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_catalogo
        FOREIGN KEY (catalogo_item_id) REFERENCES catalogo_items (id),
    CONSTRAINT fk_reservas_operador
        FOREIGN KEY (operador_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_prestamo
        FOREIGN KEY (prestamo_id) REFERENCES prestamos (id)
);

CREATE INDEX idx_reservas_prestatario ON reservas (prestatario_id);
CREATE INDEX idx_reservas_estado ON reservas (estado);
CREATE INDEX idx_reservas_catalogo ON reservas (catalogo_item_id);
