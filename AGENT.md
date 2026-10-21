# AGENT.md — Guía para trabajar en este repositorio

Este archivo está pensado para futuros agentes de IA y para cualquier desarrollador que
retome el proyecto. Resume las reglas que **no** se deben romper y los comandos que sí
funcionan.

---

## 1. Estado del proyecto

- Corte 1 completado: dominio + aplicación + API REST con persistencia **in-memory**.
- 57 tests verdes.
- El legacy está en el tag `legacy-v0` y en `docs/legacy/`; **no lo borres**.
- Rama de trabajo actual: `rewrite/v2` (se hará fast-forward a `main` al cerrar el corte).

---

## 2. Comandos

```bash
cd backend
export JAVA_HOME=/home/lizzz/tools/jdk-21.0.12.1+1   # o tu JDK 21
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw clean compile      # debe terminar en BUILD SUCCESS
./mvnw test               # 57 tests, 0 fallos
./mvnw spring-boot:run    # API en :8080, perfil demo
```

Health check:

```bash
curl -s http://localhost:8080/actuator/health
```

Demo funcional: [`docs/demo.md`](docs/demo.md).

---

## 3. Reglas duras

1. **No importes Spring/JPA/Hibernate/Jackson en `domain`.** ArchUnit falla el build.
2. **No agregues `stockTotal` ni `stockDisponible` al catálogo.** La unidad prestable es
   `Ejemplar`; la disponibilidad se deriva.
3. **No uses `UUID` sueltos en el dominio.** Usa `UsuarioId`, `CatalogoItemId`,
   `EjemplarId`, `PrestamoId`, `SancionId`.
4. **Los repositorios son puertos del dominio** en `domain/repository/`. Las
   implementaciones van en `infrastructure/`.
5. **Las reglas de negocio viven en agregados o políticas**, no en controladores ni en
   casos de uso. Un caso de uso orquesta; no decide si un préstamo es válido.
6. **No pongas `@Service`, `@Component` ni `@Repository` en `domain`.**
7. **No hay prefijo `I` para interfaces.**
8. **No borres ni reescribas el legacy** sin dejar constancia; está tags/archivado.
9. **No introduzcas JPA todavía** sin actualizar el ADR-004 y el `pom.xml` de forma
   coherente.
10. **Mantén los tests pasando** antes de cada commit.

---

## 4. Mapa del backend

```text
com.biblioteca
├── shared/domain          # AggregateRoot, ValueObject, DomainEvent, excepciones
├── shared/application     # DomainEventPublisher (puerto)
├── catalog                # CatalogoItem + DetalleItem sellado
├── inventory              # Ejemplar + Barcode + Ubicacion
├── identity               # Usuario + estados + permisos
├── circulation            # Prestamo + PeriodoPrestamo + PoliticaPrestamo
├── penalties              # Sancion + PoliticaSancion
└── infrastructure
    ├── config             # Beans, demo data, CORS
    ├── events             # SpringDomainEventPublisher
    ├── persistence        # Adaptadores in-memory
    └── web                # Controladores REST + ProblemDetail
```

Cada contexto de negocio sigue:

```text
<contexto>/
├── domain/
│   ├── model/       # agregados, VOs, enums
│   ├── event/       # eventos
│   ├── policy/      # reglas configurables
│   ├── repository/  # puertos
│   └── vo/          # IDs tipados
└── application/
    ├── command/     # intención de escritura
    └── usecase/     # orquestación
```

---

## 5. Cómo agregar una funcionalidad

Ejemplo: reservas.

1. Decide el contexto dueño (probablemente `circulation` o un nuevo `reservations`).
2. Modela el agregado y sus invariantes en `domain/model`. Usa `AggregateRoot`.
3. Crea los value objects como `record` y valida en el constructor compacto.
4. Define el puerto en `domain/repository` (interfaz, sin `I`).
5. Crea el comando en `application/command`.
6. Crea el caso de uso en `application/usecase` con `@Service`, inyectando puertos.
7. Implementa el adaptador in-memory en `infrastructure/persistence/inmemory`.
8. Expón el endpoint en `infrastructure/web/rest`.
9. Agrega tests: dominio, caso de uso y, si aplica, flujo.
10. Actualiza `README.md`, `docs/arquitectura.md` y el ADR si cambia una decisión.

---

## 6. Dónde están las reglas críticas

| Regla | Archivo |
|---|---|
| Estados y permisos de cuenta | `identity/domain/model/Usuario.java` |
| Autopréstamo y devolución tardía | `circulation/domain/model/Prestamo.java` |
| Plazos, domicilio, máximos | `circulation/domain/policy/PoliticaPrestamoEstandar.java` |
| Orquestación de préstamo | `circulation/application/usecase/RegistrarPrestamoUseCase.java` |
| Sanción automática | `penalties/application/listener/PrestamoDevueltoConRetrasoListener.java` |
| Vigencia de sanción | `penalties/domain/model/Sancion.java` |
| Disponibilidad de ejemplar | `inventory/domain/model/Ejemplar.java` |
| Datos demo | `infrastructure/config/DemoDataConfig.java` |
| Errores HTTP | `infrastructure/web/error/GlobalExceptionHandler.java` |

---

## 7. Pruebas

```bash
./mvnw test
```

- `ArquitecturaTest` es el guardián de las reglas de dependencia. Si falla, no lo
  desactives: corrige la dependencia.
- `FlujoPrestamoTest` prueba el flujo completo sin Spring. Es el test más valioso para la
  sustentación.
- Los tests de aplicación usan los adaptadores in-memory reales; no los reemplaces por
  mocks salvo que haya una razón fuerte.
- Los IDs y fechas de fixtures están en `src/test/java/com/biblioteca/support/Fixtures.java`.
- El reloj se inyecta (`Clock`) para que las pruebas sean deterministas.

---

## 8. Errores comunes a evitar

- **Poner lógica de negocio en el controlador.** El controlador solo mapea DTOs y llama
  al caso de uso.
- **Validar disponibilidad con contadores.** Usa `Ejemplar.estaDisponible()` y
  `PrestamoRepository.existeActivoPara`.
- **Guardar “vencido” como estado.** Se deriva con `PeriodoPrestamo.estaVencido(hoy)`.
- **Crear una sanción dos veces.** El listener verifica `existePorPrestamo`.
- **Usar `LocalDate.now()` directo en dominio.** Inyecta `Clock` o pasa la fecha.
- **Olvidar `extraerEventos()` después de guardar.** El caso de uso debe publicar los
  eventos del agregado.
- **Romper el orden de validaciones de `RegistrarPrestamoUseCase`.** Los tests dependen de
  los códigos de error esperados.
- **Agregar dependencias sin actualizar el `pom.xml` y el ADR correspondiente.**

---

## 9. Siguiente fase sugerida

1. **Persistencia:** JPA + PostgreSQL + Flyway + Testcontainers. Mantener los puertos.
2. **Outbox:** tabla de eventos y publicación confiable; el listener pasa a
   `@TransactionalEventListener`.
3. **Seguridad:** Spring Security + JWT; el operador sale del token, no del body.
4. **Frontend:** migrar el prototipo React legacy a React + TypeScript + Vite consumiendo
   `/api/v1`; manejar `ProblemDetail.codigo`.
5. **Reservas:** si el alcance lo confirma, nuevo contexto `reservations` con expiración.
6. **Operación:** CI (build + tests + ArchUnit), métricas, logs estructurados, despliegue.

---

## 10. Notas de entorno

- No hay Java del sistema; usa el JDK 21 indicado o instala Temurin.
- El wrapper de Maven ya está en `backend/mvnw`; no requiere Maven global.
- GitHub directo puede fallar en este entorno; `codeload.github.com` funcionó para bajar
  el proyecto original.
- El repo no tiene credenciales de push; el usuario debe subir los cambios.
