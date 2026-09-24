CREATE TYPE persona_estado AS ENUM ('ACTIVA','SUSPENDIDA', 'INHABILITADA');

CREATE TABLE persona (
	id_persona UUID PRIMARY KEY,
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
	id_admin UUID PRIMARY KEY, 
	permisos JSONB NOT NULL DEFAULT '[]'::jsonb,

	CONSTRAINT fk_admin_persona
		FOREIGN KEY (id_admin)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE perfil (
	id_perfil UUID PRIMARY KEY, 
	prestamo_domicilio_equipos BOOLEAN NOT NULL DEFAULT true,
	plazo_extendido_libros BOOLEAN NOT NULL DEFAULT true,
	cantidad_maxima_simultanea SMALLINT NOT NULL DEFAULT 2,

	CONSTRAINT fk_prefil_persona
		FOREIGN KEY (id_perfil)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE alumno (
	id_alumno UUID PRIMARY KEY, 
	cui VARCHAR(8) NOT NULL UNIQUE,

	CONSTRAINT fk_alumno_persona
		FOREIGN KEY (id_alumno)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE docente (
	id_docente UUID PRIMARY KEY, 
	departamento VARCHAR(30) NOT NULL,
	categoria VARCHAR(30) NOT NULL,

	CONSTRAINT fk_docente_persona
		FOREIGN KEY (id_docente)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);

CREATE TABLE administrativo (
	id_administrativo UUID PRIMARY KEY, 
	area VARCHAR(30) NOT NULL,

	CONSTRAINT fk_docente_persona
		FOREIGN KEY (id_docente)
		REFERENCES persona (id_persona)
		ON DELETE CASCADE
);
















