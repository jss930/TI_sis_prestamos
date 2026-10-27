# ADR-013: Revisar cuentas antes de habilitar préstamos

Estado: aceptado.

## Contexto

La validación de una cuenta era una acción inmediata sin revisión visible ni identificación del operador que la aprobaba. Faltaban un código institucional y un canal para solicitar y enviar correcciones.

## Decisión

Se conserva `PROVISIONAL` como estado del dominio y se presenta como Pendiente de validación. Las correcciones se modelan con una observación, revisor, fecha y un indicador de corrección pendiente; el reenvío elimina el bloqueo de corrección y deja la cuenta pendiente de nueva revisión. Los datos solo son editables mientras la cuenta está pendiente.

Se añade el value object `CodigoInstitucional`, requerido en los registros nuevos y antes de validar una cuenta. Es único en base de datos y se normaliza a mayúsculas. V12 no inventa códigos ni operadores para cuentas anteriores.

La validación requiere un revisor autorizado distinto del titular, confirmación explícita de revisión y ausencia de correcciones pendientes. Se bloquea la fila del usuario y se guarda quién validó y cuándo usando `Clock`. El administrador puede validar con su permiso general; también puede hacerlo una cuenta validada con el permiso específico existente.

La corrección de datos usa la identidad autenticada y solo está permitida al titular o a un revisor autorizado. Código y observaciones no se publican en las consultas anónimas o de personas ajenas a la revisión. El inicio de sesión comparte el mismo DTO de usuario para mantener contratos consistentes.

La revisión administrativa y la prueba de control del buzón son comprobaciones distintas. Sin un servicio de envío configurado, esta entrega no simula verificación por correo ni marca direcciones como verificadas.

## Consecuencias

Personas dispone de filtros de revisión y acciones para validar o solicitar correcciones. Mi cuenta permite leer observaciones y reenviar datos. La validación habilita préstamos según las reglas existentes; no concede permisos operativos ni modifica responsabilidades. No se añaden dependencias externas.
