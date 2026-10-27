-- Separar aprobacion (apartado) de entrega (prestamo). Se conserva ACEPTADA como estado historico.
ALTER TABLE catalogo_items ADD COLUMN area VARCHAR(30);
UPDATE catalogo_items SET area = CASE WHEN tipo = 'LIBRO' THEN 'BIBLIOTECA' WHEN tipo = 'OTRO' THEN 'DEPORTES' ELSE 'TECNOLOGIA' END;
UPDATE catalogo_items SET area = 'COMPUTACION' WHERE id IN (
'00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000114',
'00000000-0000-0000-0000-000000000115', '00000000-0000-0000-0000-000000000116',
'00000000-0000-0000-0000-000000000117', '00000000-0000-0000-0000-000000000118',
'00000000-0000-0000-0000-000000000119');
ALTER TABLE catalogo_items ALTER COLUMN area SET NOT NULL;
ALTER TABLE catalogo_items ALTER COLUMN area SET DEFAULT 'TECNOLOGIA';
ALTER TABLE reservas ADD COLUMN ejemplar_id UUID REFERENCES ejemplares(id);
ALTER TABLE reservas ADD COLUMN fecha_limite_retiro TIMESTAMP WITH TIME ZONE;
ALTER TABLE reservas ADD COLUMN fecha_entrega TIMESTAMP WITH TIME ZONE;
ALTER TABLE reservas ADD COLUMN entregado_por_id UUID REFERENCES usuarios(id);
ALTER TABLE reservas ADD COLUMN lugar_uso VARCHAR(20) NOT NULL DEFAULT 'EN_CAMPUS';
CREATE INDEX idx_reservas_retiro ON reservas(estado, fecha_limite_retiro);
-- Las asignaciones ya existentes tambien habilitan la bandeja; sin ampliar areas.
UPDATE usuarios SET permisos = CASE WHEN permisos = '' THEN 'GESTIONAR_RESERVAS'
ELSE CONCAT(permisos, ',GESTIONAR_RESERVAS') END
WHERE estado = 'VALIDADO' AND permisos NOT LIKE '%GESTIONAR_RESERVAS%'
AND id IN (SELECT usuario_id FROM responsabilidades_usuarios WHERE categorias <> '');
