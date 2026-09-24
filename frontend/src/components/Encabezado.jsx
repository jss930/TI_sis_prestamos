const titulos = {
  resumen: ['Resumen general', 'Consulta la actividad de préstamos de hoy.'],
  personas: ['Personas', 'Alumnos, docentes y administrativos.'],
  catalogo: ['Catálogo de bienes', 'Libros, equipos y material deportivo.'],
  prestamos: ['Préstamos', 'Registra entregas y devoluciones.'],
  reservas: ['Reservas', 'Solicitudes en espera de disponibilidad.'],
  sanciones: ['Sanciones', 'Restricciones informadas por el sistema.'],
  administracion: ['Panel administrador', 'Configuración y estado del sistema.'],
}
export default function Encabezado({ pagina }) {
  const [titulo, subtitulo] = titulos[pagina]
  return <header className="encabezado"><div><h1>{titulo}</h1><p>{subtitulo}</p></div><div className="fecha">24 SEP 2026</div></header>
}
