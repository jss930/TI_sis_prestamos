/*
cambios hechos del DDD(modelo de dominio)
persona:
	value object de contacto se declara directamente
	en los atributos de persona


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