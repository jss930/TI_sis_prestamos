import type { Pagina } from '../types'

const TITULOS: Record<Pagina, [string, string]> = {
  resumen: ['Resumen general', 'Consulta la actividad de préstamos de hoy.'],
  'mi-cuenta': ['Mi cuenta', 'Consulta el estado y los datos de tu cuenta institucional.'],
  personas: ['Personas', 'Gestiona las cuentas de nuestra comunidad universitaria.'],
  catalogo: ['Catálogo de bienes', 'Libros, equipos y otros recursos disponibles.'],
  'mis-prestamos': ['Mis préstamos', 'Historial de préstamos de tu cuenta.'],
  'mis-reservas': ['Mis solicitudes', 'Aprobación, recogida y estado de tus solicitudes.'],
  'reservas': ['Solicitudes pendientes', 'Aprueba solicitudes de tus áreas y confirma la entrega física.'],
  prestamos: ['Préstamos', 'Registra entregas y devoluciones.'],
  sanciones: ['Sanciones', 'Restricciones informadas por el sistema.'],
  administracion: ['Panel administrador', 'Administra los recursos y la configuración de la biblioteca.'],
  'catalogo-gestion': ['Gestión del catálogo', 'Administra títulos y ejemplares.'],
  'catalogo-nuevo': ['Nuevo título', 'Registra un nuevo recurso.'],
  'catalogo-editar': ['Editar título', 'Actualiza la información principal.'],
  'ejemplar-nuevo': ['Nuevo ejemplar', 'Registra una copia física.'],
  'reserva-nueva': ['Nueva reserva', 'Selecciona un horario.'],
  'prestamo-nuevo': ['Nuevo préstamo', 'Registra una entrega presencial.'],
}

interface Props {
  pagina: Pagina
  publica?: boolean
}

export default function Encabezado({ pagina, publica = false }: Props) {
  const [titulo, subtitulo] = TITULOS[pagina]


  return (
    <header className="encabezado">
      <div>
        <p className="sobrelinea">BIBLIOTECA · UNSA</p>
        <h1>{publica ? 'Catálogo público' : titulo}</h1>
        <p>{publica ? 'Explora libros, equipos y recursos para seguir aprendiendo.' : subtitulo}</p>
      </div>
    </header>
  )
}
