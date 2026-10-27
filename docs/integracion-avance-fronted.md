# Integración del avance de `fronted` en TypeScript

Base: `frontend-actualizado`, sobre `origin/main` 009bcdd. Se recuperan los flujos de la rama
`fronted` a3583e5 usando React/TypeScript y la API existente. No se mezclan los datos demo
numéricos del navegador con los UUID reales de PostgreSQL.

## Funciones y dónde usarlas

- **Catálogo:** búsqueda por título, ISBN, UUID del bien o código de inventario; combina con el filtro por tipo.
- **Solicitudes y reservas:** filtros por estado, búsqueda por persona/bien, revisión, motivo de denegación
  y selección explícita del ejemplar que se entrega. Aceptar registra el préstamo en una operación.
- **Préstamos:** selección de bien/ejemplar y formulario de inspección antes de recibir una devolución.
  Registra estado físico, accesorios faltantes, observaciones, fecha y cuenta que recibió el bien.
- **Administración:** asignación de Biblioteca, Deportes, Tecnología y Computación; capacitación técnica
  e historial de asignaciones y revocaciones con actor y fecha.

Las asignaciones por categoría no conceden permisos operativos. Solo ADMINISTRAR_SISTEMA identifica a la cuenta administradora; el perfil ALUMNO, DOCENTE o ADMINISTRATIVO no concede módulos por sí mismo. Los permisos se gestionan en Personas y solo el administrador puede otorgarlos o revocarlos.
Las categorías técnicas requieren capacitación verificada. Retirar la capacitación retira también
las asignaciones técnicas y registra cada cambio. Cuentas provisionales o suspendidas no reciben nuevas
asignaciones; se permite retirar las existentes.

Una inspección con daños, pérdida o accesorios faltantes requiere observaciones (máximo 1000 caracteres).
La recepción cierra el préstamo; el ejemplar queda DISPONIBLE, DANADO o PERDIDO según la inspección.
Si está físicamente bueno pero faltan accesorios, queda EN_REVISION y no puede prestarse.
El historial conserva el resultado de la inspección, aunque el inventario cambie posteriormente.
No se añadió una operación de reparación/liberación de revisión.

## Persistencia y compatibilidad

`backend/src/main/resources/db/migration/V4__responsables_e_inspecciones.sql` y `V5__separar_administrador.sql` añaden:

- `responsabilidades_usuarios`: capacitación y categorías por UUID de usuario.
- `historial_responsables`: eventos de asignación, retirada y capacitación.
- `inspecciones_devolucion`: una inspección por préstamo, con receptor, estado, observaciones y fecha.

No altera las migraciones V1–V3 ni borra o reemplaza registros existentes.
La devolución y su inspección se guardan en una misma transacción. Se bloquea el préstamo durante la
recepción y el usuario durante los cambios de asignaciones para serializar modificaciones simultáneas.
Se mantienen adaptadores JPA y en memoria dentro de la arquitectura existente.

Los endpoints nuevos exigen el token de login. El actor del historial se obtiene del token, no del cuerpo
que envía el navegador. Responsables requiere ADMINISTRAR_SISTEMA; inspecciones requiere REGISTRAR_DEVOLUCION,
siempre con cuenta validada. Esto no cambia el control de acceso de los endpoints antiguos.

| Endpoint | Función |
| --- | --- |
| GET `/api/v1/responsables` | Responsables e historial |
| PUT `/api/v1/responsables/{usuarioId}/capacitacion` | `{ "verificada": true }` |
| PUT `/api/v1/responsables/{usuarioId}/categorias/{categoria}` | `{ "asignada": true }` |
| GET `/api/v1/inspecciones-devolucion` | Historial de recepciones |
| POST `/api/v1/prestamos/{id}/inspeccion-devolucion` | Estado físico, accesoriosFaltantes y observaciones |
| POST `/api/v1/reservas/{id}/aceptar` | Admite `ejemplarId` opcional junto a `operadorId` |

La aceptación sin `ejemplarId` mantiene la selección automática anterior. La devolución antigua
`POST /prestamos/{id}/devolucion` sigue disponible para compatibilidad; esas devoluciones no crean una
inspección retroactiva. La interfaz nueva utiliza el endpoint de inspección.
Los datos guardados antes en localStorage por la versión JSX no se importan automáticamente: sus IDs
numéricos y personas demo no corresponden a las cuentas reales. Las claves antiguas no se eliminan.

## Comprobación

- Backend: `cd backend` y `mvn test` — 99 pruebas, incluidas 17 de integración nuevas.
- Frontend: `cd frontend` y `npm run build` — TypeScript estricto y compilación Vite.
- Navegador: login, capacitación, asignación técnica, revocación, persistencia al recargar,
  aceptación de reserva con ejemplar elegido e inspección con faltantes sobre un backend H2 temporal.
- La prueba temporal usa URL y driver H2 explícitos; no basta activar el perfil test en `spring-boot:run`
  si los recursos de test no están en el classpath de la aplicación.
- PostgreSQL: comprobar las cuatro migraciones y arrancar con `mvn spring-boot:run`.

## Archivos de implementación modificados o añadidos

La lista incluye la adaptación TypeScript del turno anterior. Se conservan sin tocar los archivos
preexistentes sin seguimiento `frontend/nul` y `frontend/package-lock.json`.

- `backend/src/main/java/com/biblioteca/circulation/application/usecase/InspeccionarDevolucionUseCase.java`
- `backend/src/main/java/com/biblioteca/circulation/domain/model/InspeccionDevolucion.java`
- `backend/src/main/java/com/biblioteca/circulation/domain/model/Prestamo.java`
- `backend/src/main/java/com/biblioteca/circulation/domain/repository/InspeccionDevolucionRepository.java`
- `backend/src/main/java/com/biblioteca/circulation/domain/repository/PrestamoRepository.java`
- `backend/src/main/java/com/biblioteca/identity/application/usecase/GestionarResponsablesUseCase.java`
- `backend/src/main/java/com/biblioteca/identity/domain/model/CambioResponsable.java`
- `backend/src/main/java/com/biblioteca/identity/domain/model/CategoriaResponsable.java`
- `backend/src/main/java/com/biblioteca/identity/domain/model/ResponsabilidadUsuario.java`
- `backend/src/main/java/com/biblioteca/identity/domain/repository/ResponsabilidadRepository.java`
- `backend/src/main/java/com/biblioteca/identity/domain/repository/UsuarioRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/inmemory/InMemoryInspeccionDevolucionRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/inmemory/InMemoryResponsabilidadRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/adapter/InspeccionDevolucionJpaAdapter.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/adapter/PrestamoJpaAdapter.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/adapter/ResponsabilidadJpaAdapter.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/adapter/UsuarioJpaAdapter.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/entity/CambioResponsableJpaEntity.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/entity/InspeccionDevolucionJpaEntity.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/entity/ResponsabilidadUsuarioJpaEntity.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/repository/CambioResponsableJpaRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/repository/InspeccionDevolucionJpaRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/repository/PrestamoJpaRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/repository/ResponsabilidadUsuarioJpaRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/persistence/jpa/repository/UsuarioJpaRepository.java`
- `backend/src/main/java/com/biblioteca/infrastructure/reservations/CirculationRegistrarPrestamoAdapter.java`
- `backend/src/main/java/com/biblioteca/infrastructure/security/UsuarioAutenticado.java`
- `backend/src/main/java/com/biblioteca/infrastructure/web/error/GlobalExceptionHandler.java`
- `backend/src/main/java/com/biblioteca/infrastructure/web/rest/InspeccionDevolucionController.java`
- `backend/src/main/java/com/biblioteca/infrastructure/web/rest/ReservaController.java`
- `backend/src/main/java/com/biblioteca/infrastructure/web/rest/ResponsableController.java`
- `backend/src/main/java/com/biblioteca/inventory/domain/model/Ejemplar.java`
- `backend/src/main/java/com/biblioteca/inventory/domain/model/EstadoFisico.java`
- `backend/src/main/java/com/biblioteca/reservations/application/command/AceptarReservaCommand.java`
- `backend/src/main/java/com/biblioteca/reservations/application/port/RegistrarPrestamoPort.java`
- `backend/src/main/java/com/biblioteca/reservations/application/usecase/AceptarReservaUseCase.java`
- `backend/src/main/resources/db/migration/V4__responsables_e_inspecciones.sql`
- `backend/src/test/java/com/biblioteca/infrastructure/web/ResponsablesInspeccionesTest.java`
- `frontend/src/App.tsx`
- `frontend/src/components/BarraLateral.tsx`
- `frontend/src/components/Encabezado.tsx`
- `frontend/src/components/EtiquetaEstado.tsx`
- `frontend/src/components/FormularioInspeccion.tsx`
- `frontend/src/components/GestionResponsables.tsx`
- `frontend/src/pages/Administracion.tsx`
- `frontend/src/pages/Catalogo.tsx`
- `frontend/src/pages/Prestamos.tsx`
- `frontend/src/pages/Reservas.tsx`
- `frontend/src/services/api.ts`
- `frontend/src/styles.css`
- `frontend/src/types.ts`

## Modelo de acceso aplicado

`ADMINISTRAR_SISTEMA` identifica exclusivamente al administrador. `REGISTRAR_PRESTAMO` y `REGISTRAR_DEVOLUCION` son independientes y pueden entregarse a cualquier perfil validado. `GESTIONAR_RESERVAS`, `GESTIONAR_CATALOGO`, `GESTIONAR_SANCIONES`, `VALIDAR_USUARIO_PROVISIONAL` y `GESTIONAR_USUARIOS` se delegan según la responsabilidad concreta. Una cuenta sin permisos operativos conserva catálogo, solicitudes propias y sus propios préstamos/reservas.
