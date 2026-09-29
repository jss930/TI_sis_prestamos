export const datosDemo = {
  personas: [
    { id: 1, nombre: 'Ana Torres', codigo: '20230124', tipo: 'Alumno', estado: 'Activo' },
    { id: 2, nombre: 'Marco Salas', codigo: 'DOC-018', tipo: 'Docente', estado: 'Activo' },
    { id: 3, nombre: 'Lucía Ramos', codigo: 'ADM-007', tipo: 'Administrativo', estado: 'Suspendido' },
  ],
  items: [
    { id: 1, nombre: 'Introducción a Java', categoria: 'Libro', codigo: 'LIB-104', disponibles: 3 },
    { id: 2, nombre: 'Laptop Lenovo ThinkPad', categoria: 'Equipo', codigo: 'LAP-012', disponibles: 1 },
    { id: 3, nombre: 'Lentes inteligentes', categoria: 'Equipo', codigo: 'LEN-004', disponibles: 0 },
    { id: 4, nombre: 'Mesa de tenis', categoria: 'Deporte', codigo: 'DEP-021', disponibles: 1 },
    { id: 5, nombre: 'Pelota de vóley', categoria: 'Deporte', codigo: 'DEP-035', disponibles: 6 },
  ],
  prestamos: [
    { id: 101, persona: 'Ana Torres', item: 'Introducción a Java', entrega: '20/09/2026', devolucion: '27/09/2026', estado: 'Vigente' },
    { id: 102, persona: 'Marco Salas', item: 'Laptop Lenovo ThinkPad', entrega: '15/09/2026', devolucion: '22/09/2026', estado: 'Vencido' },
  ],
  reservas: [
    { id: 201, persona: 'Ana Torres', item: 'Lentes inteligentes', fecha: '23/09/2026', estado: 'En espera' },
  ],
  sanciones: [
    { id: 301, persona: 'Lucía Ramos', motivo: 'Devolución tardía', desde: '18/09/2026', hasta: '02/10/2026', estado: 'Activa' },
  ],
}
