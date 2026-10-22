-- Las responsabilidades activas habilitan la atención de préstamos y devoluciones.
-- Solo se sincronizan cuentas validadas; no se eliminan permisos administrativos.
UPDATE usuarios u
SET permisos = concat_ws(',',
    NULLIF(u.permisos, ''),
    CASE WHEN position('REGISTRAR_PRESTAMO' IN u.permisos) = 0 THEN 'REGISTRAR_PRESTAMO' END,
    CASE WHEN position('REGISTRAR_DEVOLUCION' IN u.permisos) = 0 THEN 'REGISTRAR_DEVOLUCION' END)
WHERE u.estado = 'VALIDADO'
  AND EXISTS (
      SELECT 1 FROM responsabilidades_usuarios r
      WHERE r.usuario_id = u.id AND trim(r.categorias) <> ''
  );
