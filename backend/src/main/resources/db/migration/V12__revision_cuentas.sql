-- Los datos historicos no se inventan: codigo y revisor quedan nulos si no se registraron.
ALTER TABLE usuarios ADD COLUMN codigo_institucional VARCHAR(30);
ALTER TABLE usuarios ADD CONSTRAINT uq_usuario_codigo_institucional UNIQUE(codigo_institucional);
ALTER TABLE usuarios ADD COLUMN validado_por_id UUID REFERENCES usuarios(id);
ALTER TABLE usuarios ADD COLUMN observaciones_revision VARCHAR(1000);
ALTER TABLE usuarios ADD COLUMN revisado_por_id UUID REFERENCES usuarios(id);
ALTER TABLE usuarios ADD COLUMN fecha_revision TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuarios ADD COLUMN correccion_pendiente BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_usuarios_revision ON usuarios(estado, correccion_pendiente);
