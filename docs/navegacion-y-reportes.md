# Navegación, actividad personal y reportes

## Menú por tareas

- Resumen y Catálogo.
- Atención agrupa Reservas y Préstamos/devoluciones según permisos.
- Personas incluye una pestaña de sanciones y accesos por persona.
- Reportes está reservado al administrador; el historial propio se abre desde
  Mi actividad, junto a las pestañas de préstamos activos y reservas.
- El nombre/avatar inferior abre Mi cuenta. Administración no repite accesos
  en el menú; su ruta anterior conserva la información de referencia.

Las rutas anteriores de Mis préstamos, Mis reservas y Sanciones se mantienen
para enlaces existentes, pero no duplican entradas del menú. Solo hay una
página activa. Los botones/enlaces usan distribución flex, altura mínima de
44 px y espacios explícitos entre acciones, texto y tablas.

## Administrador como prestatario

El permiso administrativo no sustituye el perfil de préstamo ni concede
automáticamente todos los permisos operativos. Una cuenta administradora
validada puede reservar y recibir préstamos con los plazos de su perfil.
Otro operador autorizado debe aprobar/entregar: no se permite autopréstamo.

## Reportes y resultados recientes

Reportes ofrece General, Por usuario y Por ejemplar. La vista general requiere
administrador; el historial propio mantiene su autorización por titular.
La pantalla empieza con diez registros por sección y permite Ver 10 más.
Los préstamos se consultan con paginación y filtros en PostgreSQL/JPA, con orden
estable por fecha de entrega e id. Los indicadores usan todo el filtro.
Reservas y sanciones se recortan en el servidor antes de responder.

La búsqueda por nombre, correo, código institucional, título o barcode y el
estado solo filtran préstamos. Las fechas son inclusivas en Lima para cada
sección. Sin fechas se indica “Todo el historial disponible”.

Imprimir / guardar PDF solicita expresamente el reporte completo filtrado,
independientemente de las páginas abiertas. No se imprimen menú, filtros ni
botones. Sanciones comparte la misma cabecera y estilo de impresión.
Los listados de Atención y Mi actividad muestran préstamos activos; los
cerrados y sus inspecciones permanecen en Reportes, sin cargar todo el historial
al iniciar la aplicación.

API: `GET /api/v1/reportes/general`, `/usuarios/{id}`, `/ejemplares/{id}`.
Parámetros: `desde`, `hasta`, `estado`, `busqueda`, `pagina` (desde cero),
`tamano` (1–50, por defecto 10), `completo` (por defecto false).
