-- Datos adicionales: no modifica ni elimina registros anteriores.
CREATE TABLE responsabilidades_usuarios (
    usuario_id UUID PRIMARY KEY REFERENCES usuarios(id),
    capacitacion_verificada BOOLEAN NOT NULL DEFAULT FALSE,
    categorias VARCHAR(200) NOT NULL DEFAULT ''
);

CREATE TABLE historial_responsables (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    operador_id UUID NOT NULL REFERENCES usuarios(id),
    categoria VARCHAR(30),
    accion VARCHAR(40) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_historial_responsables_usuario ON historial_responsables(usuario_id);
CREATE INDEX idx_historial_responsables_fecha ON historial_responsables(fecha);

CREATE TABLE inspecciones_devolucion (
    prestamo_id UUID PRIMARY KEY REFERENCES prestamos(id),
    recibido_por_id UUID NOT NULL REFERENCES usuarios(id),
    estado_fisico VARCHAR(20) NOT NULL,
    accesorios_faltantes BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones VARCHAR(1000) NOT NULL DEFAULT '',
    fecha TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_inspeccion_estado CHECK (estado_fisico IN ('DISPONIBLE', 'DANADO', 'PERDIDO')),
    CONSTRAINT ck_inspeccion_observaciones CHECK (
        (estado_fisico = 'DISPONIBLE' AND accesorios_faltantes = FALSE) OR LENGTH(TRIM(observaciones)) > 0)
);
CREATE INDEX idx_inspecciones_fecha ON inspecciones_devolucion(fecha);
