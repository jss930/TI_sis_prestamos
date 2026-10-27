-- El administrador es una cuenta explícita; los permisos operativos no lo convierten en administrador.
UPDATE usuarios
SET permisos = CASE
    WHEN permisos IS NULL OR permisos = '' THEN 'ADMINISTRAR_SISTEMA,REGISTRAR_DEVOLUCION'
    WHEN position('ADMINISTRAR_SISTEMA' IN permisos) > 0 THEN permisos
    ELSE permisos || ',ADMINISTRAR_SISTEMA,REGISTRAR_DEVOLUCION'
END
WHERE email = 'bibliotecario@biblioteca.pe';
