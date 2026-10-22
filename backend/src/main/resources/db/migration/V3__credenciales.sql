CREATE TABLE credenciales (
    usuario_id UUID PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT fk_credenciales_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
