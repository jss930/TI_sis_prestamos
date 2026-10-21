-- Fase 2: esquema inicial de los cinco bounded contexts.
-- Las referencias entre contextos son IDs, no relaciones de dominio.

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    permisos VARCHAR(500) NOT NULL DEFAULT '',
    fecha_alta TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_validacion TIMESTAMP WITH TIME ZONE,
    motivo_suspension VARCHAR(500)
);

CREATE TABLE catalogo_items (
    id UUID PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL,
    fecha_alta TIMESTAMP WITH TIME ZONE NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    autor VARCHAR(120),
    editorial VARCHAR(150),
    anio_publicacion INTEGER,
    marca VARCHAR(120),
    modelo VARCHAR(120),
    descripcion VARCHAR(500)
);

CREATE TABLE ejemplares (
    id UUID PRIMARY KEY,
    catalogo_item_id UUID NOT NULL,
    barcode VARCHAR(50) NOT NULL UNIQUE,
    sala VARCHAR(50) NOT NULL,
    estante VARCHAR(50) NOT NULL,
    estado_fisico VARCHAR(20) NOT NULL,
    notas VARCHAR(500),
    fecha_alta TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ejemplares_catalogo
        FOREIGN KEY (catalogo_item_id) REFERENCES catalogo_items (id)
);

CREATE INDEX idx_ejemplares_catalogo ON ejemplares (catalogo_item_id);

CREATE TABLE prestamos (
    id UUID PRIMARY KEY,
    operador_id UUID NOT NULL,
    prestatario_id UUID NOT NULL,
    ejemplar_id UUID NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_devolucion DATE,
    lugar_uso VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    renovaciones INTEGER NOT NULL,
    observaciones VARCHAR(500),
    fecha_alta TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prestamos_operador
        FOREIGN KEY (operador_id) REFERENCES usuarios (id),
    CONSTRAINT fk_prestamos_prestatario
        FOREIGN KEY (prestatario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_prestamos_ejemplar
        FOREIGN KEY (ejemplar_id) REFERENCES ejemplares (id)
);

CREATE INDEX idx_prestamos_prestatario ON prestamos (prestatario_id);
CREATE INDEX idx_prestamos_ejemplar ON prestamos (ejemplar_id);
CREATE INDEX idx_prestamos_estado ON prestamos (estado);

CREATE TABLE sanciones (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    prestamo_id UUID,
    motivo VARCHAR(20) NOT NULL,
    dias_suspension INTEGER NOT NULL,
    multa NUMERIC(12, 2) NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(500),
    CONSTRAINT fk_sanciones_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_sanciones_prestamo
        FOREIGN KEY (prestamo_id) REFERENCES prestamos (id)
);

CREATE INDEX idx_sanciones_usuario ON sanciones (usuario_id);
CREATE INDEX idx_sanciones_estado ON sanciones (estado);
