# Guion de demostración

## 1. Preparación

Desde la raíz:

```powershell
docker compose up -d
```

Terminal del backend:

```powershell
cd backend
mvn spring-boot:run
```

Terminal del frontend:

```powershell
cd frontend
npm run dev
```

Comprobar:

- Frontend: <http://localhost:5173>
- Salud: <http://localhost:8080/actuator/health>
- Adminer: <http://localhost:8081>

## 2. Cuentas

Contraseña común: `password`.

| Correo | Uso en demo |
|---|---|
| `bibliotecario@biblioteca.pe` | Administración |
| `ana@universidad.edu.pe` | Cuenta provisional |
| `luis@universidad.edu.pe` | Alumno validado con préstamo vencido |
| `docente@universidad.edu.pe` | Docente validada |

## 3. Demostración recomendada

### Paso 1: catálogo público

Abrir el catálogo sin sesión. Mostrar búsqueda, tipos y cantidad de ejemplares disponibles.

Explicar: el catálogo describe títulos; la cantidad sale del inventario.

### Paso 2: cuenta provisional

Entrar con Ana. Mostrar `Mi cuenta` y catálogo. Intentar ir a una tarea restringida.

Explicar: puede entrar, pero todavía no reservar.

### Paso 3: validación

Entrar como bibliotecario, abrir Personas y validar una cuenta pendiente.

Explicar: validar habilita el negocio; los permisos administrativos se conceden por separado.

### Paso 4: reserva

Entrar con una cuenta validada, abrir un título y pulsar Reservar.

Mostrar:

- detalles del título;
- mínimo de 2 horas;
- jornada 07:00–20:00;
- máximo 24 horas;
- campus/domicilio para libro;
- campus fijo para equipo.

Confirmar y abrir Mis reservas.

### Paso 5: aprobación

Entrar como bibliotecario, abrir Aprobar reservas y revisar la solicitud.

Explicar: el usuario no eligió copia; el sistema la asigna tras comprobar el intervalo.

### Paso 6: entrega

Cuando el horario permita recoger, confirmar entrega.

Explicar: la reserva queda atendida, se crea el préstamo y el ejemplar queda prestado.

### Paso 7: base de datos

Abrir Adminer:

```text
Sistema: PostgreSQL
Servidor: postgres
Usuario: biblioteca
Contraseña: biblioteca
Base: biblioteca
```

Ejecutar:

```sql
SELECT id, estado, inicio_reserva, fin_reserva, ejemplar_id, prestamo_id
FROM reservas
ORDER BY fecha_alta DESC;
```

```sql
SELECT id, estado, prestatario_id, ejemplar_id,
       fecha_hora_entrega, fecha_hora_vencimiento
FROM prestamos
ORDER BY fecha_alta DESC;
```

### Paso 8: pruebas

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm run build
```

Explicar: las pruebas validan reglas, persistencia, migraciones, arquitectura y concurrencia.

## 4. Demostración de error controlado

Elegir uno:

- reservar con menos de dos horas;
- reservar fuera de 07:00–20:00;
- intentar domicilio con equipo;
- solicitar un intervalo sin copias;
- prestar una copia comprometida por reserva.

Mostrar que la API devuelve un código estable y el frontend un mensaje comprensible.

## 5. Plan alterno

Si la aplicación no inicia:

1. mostrar README y diagramas;
2. abrir `docs/base-de-datos.md`;
3. enseñar las pruebas del repositorio;
4. mostrar `.github/workflows/ci.yml`;
5. explicar el flujo con capturas o código.

## 6. Cierre

> El proyecto separa título, copia, reserva y préstamo; protege las reglas en el dominio; usa transacciones y bloqueos para concurrencia; y mantiene una base reproducible con Flyway.
