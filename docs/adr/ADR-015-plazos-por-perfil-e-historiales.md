# ADR-015: Plazos por perfil e historiales de lectura

## Decisión

Sustituye el máximo general de 24 horas y la anticipación de dos horas
documentados originalmente en ADR-014. La anticipación vigente es de 30 minutos.

Reservas y préstamos comparten `PoliticaPlazoPrestamo`: campus de una a trece
horas, mismo día y jornada 07:00–20:00 en Lima. Domicilio admite libros de alumnos
y administrativos hasta tres días, libros de docentes hasta cinco días, y
equipos de docentes/administrativos hasta tres días. Otros bienes nunca salen
del campus. Los días son períodos exactos de 24 horas.

La renovación no evita el máximo total desde la entrega original. La recogida
tardía conserva el vencimiento y necesita una hora restante.

Los historiales son modelos de lectura de operaciones persistidas. No se crea
una tabla de auditoría ni se inventan cambios que no se guardaron. El titular
consulta su historial y el administrador consulta usuarios y ejemplares.

## Consecuencias

- Se reutilizan préstamos, reservas, sanciones e inspecciones existentes.
- Filtros de fechas inclusivos en Lima; estado solo para préstamos.
- PDF mediante impresión del navegador, sin nuevas dependencias de generación.
- El historial por ejemplar solo incluye reservas con esa copia asignada.
- Las consultas agregadas son adecuadas para el volumen académico actual;
  un despliegue institucional requerirá paginación y consultas SQL optimizadas.
