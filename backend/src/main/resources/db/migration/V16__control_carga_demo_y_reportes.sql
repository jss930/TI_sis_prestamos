-- La carga demo es opcional (perfil demo), aditiva y se ejecuta una sola vez.
CREATE TABLE cargas_demo (
    clave VARCHAR(80) PRIMARY KEY,
    completada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha TIMESTAMP WITH TIME ZONE
);
INSERT INTO cargas_demo (clave, completada) VALUES ('ampliada-v1', FALSE);
CREATE INDEX idx_prestamos_entrega_id ON prestamos (fecha_hora_entrega, id);
