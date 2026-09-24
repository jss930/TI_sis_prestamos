/*
cambios hechos del DDD(modelo de dominio)
persona:
	-value object de contacto se declara directamente
	en los atributos de persona
	-se elimina el VO de tipo de persona para eviar redundacion de información, y 
	evitaba que un administrativo pueda ser docente o alumno al mismo tiempo

circulacion:
	VO periodoPrestamo este en atributos de prestamo
	

*/

-- =================== USUARIO =======================

CREATE TYPE persona_estado AS ENUM ('ACTIVA','SUSPENDIDA', 'INHABILITADA');

CREATE TABLE persona (
	id_persona BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	nombres VARCHAR(75) NOT NULL, 
	apellidos VARCHAR(75) NOT NULL,
	contacto_correo VARCHAR(255) NOT NULL,
	contacto_telefono VARCHAR(20),
	estado persona_estado NOT NULL DEFAULT 'ACTIVA',
	-- Validaciones de integridad a nivel de BD
    CONSTRAINT chk_contacto_correo CHECK (contacto_correo ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')	
);

-- Índice recomendado para búsquedas o autenticación rápida por email
CREATE UNIQUE INDEX idx_persona_contacto_correo ON persona (contacto_correo);

CREATE TABLE "admin" (
	id_admin BIGINT PRIMARY KEY, 
	permisos JSONB NOT NULL DEFAULT '[]'::jsonb,

	CONSTRAINT fk_admin_persona
		FOREIGN KEY (id_admin)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE perfil (
	id_perfil BIGINT PRIMARY KEY, 
	prestamo_domicilio_equipos BOOLEAN NOT NULL DEFAULT true,
	plazo_extendido_libros BOOLEAN NOT NULL DEFAULT true,
	cantidad_maxima_simultanea SMALLINT NOT NULL DEFAULT 2,

	CONSTRAINT fk_perfil_persona
		FOREIGN KEY (id_perfil)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE alumno (
	id_alumno BIGINT PRIMARY KEY, 
	cui VARCHAR(8) NOT NULL UNIQUE,

	CONSTRAINT fk_alumno_persona
		FOREIGN KEY (id_alumno)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE docente (
	id_docente BIGINT PRIMARY KEY, 
	departamento VARCHAR(30) NOT NULL,
	categoria VARCHAR(30) NOT NULL,

	CONSTRAINT fk_docente_persona
		FOREIGN KEY (id_docente)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE administrativo (
	id_administrativo BIGINT PRIMARY KEY, 
	area VARCHAR(30) NOT NULL,

	CONSTRAINT fk_administrativo_persona
		FOREIGN KEY (id_administrativo)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);


-- ================= INVENTARIO =====================

CREATE TYPE categoria_item AS ENUM ('LIBRO', 'EQUIPO', 'OTRO');
CREATE TYPE estado_fisico AS ENUM ('DISPONIBLE', 'DANADO', 'PERDIDO');

CREATE TABLE ubicacion_fisica (
    id_ubicacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sala VARCHAR(50) NOT NULL,
    estante VARCHAR(50) NOT NULL
);

CREATE TABLE item (
    id_item BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    categoria categoria_item NOT NULL,
    cantidad_total INTEGER NOT NULL DEFAULT 0,
    cantidad_disponible INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT chk_item_cantidad_total 
        CHECK (cantidad_total >= 0),
    CONSTRAINT chk_item_cantidad_disponible 
        CHECK (cantidad_disponible >= 0),
    CONSTRAINT chk_item_cantidad_consistente 
        CHECK (cantidad_disponible <= cantidad_total)
);

CREATE TABLE libro (
    id_item BIGINT PRIMARY KEY,
    isbn VARCHAR(18) NOT NULL UNIQUE,
    autor VARCHAR(100) NOT NULL,
    editorial VARCHAR(100) NOT NULL,
    edicion VARCHAR(30),

    CONSTRAINT fk_libro_item
        FOREIGN KEY (id_item)
        REFERENCES item (id_item)
        ON DELETE CASCADE
);

CREATE TABLE equipo (
    id_item BIGINT PRIMARY KEY,
    numero_serie VARCHAR(50) NOT NULL UNIQUE,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,

    CONSTRAINT fk_equipo_item
        FOREIGN KEY (id_item)
        REFERENCES item (id_item)
        ON DELETE CASCADE
);

CREATE TABLE ejemplar (
    id_ejemplar BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_item BIGINT NOT NULL,
    id_ubicacion BIGINT,
    codigo_inventario VARCHAR(50) NOT NULL UNIQUE,
    estado_fisico estado_fisico NOT NULL DEFAULT 'DISPONIBLE',
    disponible BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT fk_ejemplar_item
        FOREIGN KEY (id_item)
        REFERENCES item (id_item)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ejemplar_ubicacion
        FOREIGN KEY (id_ubicacion)
        REFERENCES ubicacion_fisica (id_ubicacion)
        ON DELETE SET NULL
);

CREATE UNIQUE INDEX idx_ejemplar_codigo_inventario ON ejemplar (codigo_inventario);
CREATE INDEX idx_ejemplar_item_disponible ON ejemplar (id_item, disponible);

-- ================ CIRCULACION ====================

CREATE TYPE estado_prestamo AS ENUM ('ACTIVO', 'DEVUELTO', 'VENCIDO', 'PERDIDO');
CREATE TYPE estado_reserva AS ENUM ('PENDIENTE', 'CONFIRMADA', 'EXPIRADA');
CREATE TYPE lugar_uso AS ENUM ('EN_CAMPUS', 'DOMICILIO');

CREATE TABLE prestamo (
    id_prestamo BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_persona BIGINT NOT NULL,
    id_ejemplar BIGINT NOT NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_devolucion_real TIMESTAMP WITH TIME ZONE,
    lugar_uso lugar_uso NOT NULL,
    estado estado_prestamo NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT fk_prestamo_persona
        FOREIGN KEY (id_persona)
        REFERENCES persona (id_persona)
        ON DELETE RESTRICT,

    CONSTRAINT fk_prestamo_ejemplar
        FOREIGN KEY (id_ejemplar)
        REFERENCES ejemplar (id_ejemplar)
        ON DELETE RESTRICT,

    CONSTRAINT chk_fechas_prestamo
        CHECK (fecha_vencimiento >= fecha_inicio),

    CONSTRAINT chk_fecha_devolucion
        CHECK (fecha_devolucion_real IS NULL OR fecha_devolucion_real >= fecha_inicio)
);

CREATE TABLE reserva (
    id_reserva BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_persona BIGINT NOT NULL,
    id_item BIGINT NOT NULL,
    id_ejemplar BIGINT,
    fecha_reserva TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP WITH TIME ZONE NOT NULL,
    estado estado_reserva NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT fk_reserva_persona
        FOREIGN KEY (id_persona)
        REFERENCES persona (id_persona)
        ON DELETE RESTRICT,

    CONSTRAINT fk_reserva_item
        FOREIGN KEY (id_item)
        REFERENCES item (id_item)
        ON DELETE RESTRICT,

    CONSTRAINT fk_reserva_ejemplar
        FOREIGN KEY (id_ejemplar)
        REFERENCES ejemplar (id_ejemplar)
        ON DELETE SET NULL,

    CONSTRAINT chk_fecha_expiracion
        CHECK (fecha_expiracion >= fecha_reserva)
);

CREATE INDEX idx_prestamo_persona ON prestamo (id_persona);
CREATE INDEX idx_prestamo_ejemplar ON prestamo (id_ejemplar);
CREATE INDEX idx_prestamo_estado ON prestamo (estado);
CREATE INDEX idx_reserva_persona ON reserva (id_persona);
CREATE INDEX idx_reserva_item ON reserva (id_item);
CREATE INDEX idx_reserva_estado ON reserva (estado);

-- ================ CIRCULACION ====================

CREATE TYPE motivo_sancion AS ENUM ('DEVOLUCION_TARDIA', 'DANO', 'PERDIDA');
CREATE TYPE estado_sancion AS ENUM ('ACTIVA', 'CUMPLIDA');
CREATE TYPE tipo_evento_historial AS ENUM ('CREADO', 'DEVUELTO', 'VENCIDO', 'SANCIONADO');

CREATE TABLE sancion (
    id_sancion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_persona BIGINT NOT NULL,
    id_prestamo BIGINT NOT NULL,
    motivo motivo_sancion NOT NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP WITH TIME ZONE NOT NULL,
    estado estado_sancion NOT NULL DEFAULT 'ACTIVA',

    CONSTRAINT fk_sancion_persona
        FOREIGN KEY (id_persona)
        REFERENCES persona (id_persona)
        ON DELETE RESTRICT,

    CONSTRAINT fk_sancion_prestamo
        FOREIGN KEY (id_prestamo)
        REFERENCES prestamo (id_prestamo)
        ON DELETE RESTRICT,

    CONSTRAINT chk_fechas_sancion
        CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE politica_prestamo (
    id_politica BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_persona VARCHAR(30) NOT NULL,
    tipo_item categoria_item NOT NULL,
    duracion_maxima_dias INTEGER NOT NULL,
    cantidad_maxima_simultanea INTEGER NOT NULL,
    permite_prestamo_domicilio BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT chk_duracion_maxima
        CHECK (duracion_maxima_dias > 0),

    CONSTRAINT chk_cantidad_maxima
        CHECK (cantidad_maxima_simultanea > 0),

    CONSTRAINT uq_politica_persona_item
        UNIQUE (tipo_persona, tipo_item)
);

CREATE TABLE historial_prestamo (
    id_historial BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_persona BIGINT NOT NULL,
    id_prestamo BIGINT NOT NULL,
    tipo_evento tipo_evento_historial NOT NULL,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_persona
        FOREIGN KEY (id_persona)
        REFERENCES persona (id_persona)
        ON DELETE CASCADE,

    CONSTRAINT fk_historial_prestamo
        FOREIGN KEY (id_prestamo)
        REFERENCES prestamo (id_prestamo)
        ON DELETE CASCADE
);

CREATE INDEX idx_sancion_persona ON sancion (id_persona);
CREATE INDEX idx_sancion_estado ON sancion (estado);
CREATE INDEX idx_historial_persona ON historial_prestamo (id_persona);
CREATE INDEX idx_historial_prestamo ON historial_prestamo (id_prestamo);

























