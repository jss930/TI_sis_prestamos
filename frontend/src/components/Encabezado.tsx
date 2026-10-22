import type { Pagina } from '../types'

const TITULOS: Record<Pagina, [string, string]> = {
  resumen: ['Resumen general', 'Consulta la actividad de préstamos de hoy.'],
  personas: ['Personas', 'Alumnos, docentes y administrativos registrados en la API.'],
  catalogo: ['Catálogo de bienes', 'Libros, equipos y otros recursos disponibles.'],
  'mis-prestamos': ['Mis préstamos', 'Historial de préstamos de tu cuenta.'],
  'mis-reservas': ['Mis reservas', 'Estado de tus reservas.'],
  'reservas': ['Solicitudes y reservas', 'Revisa solicitudes y consulta las decisiones registradas.'],
  prestamos: ['Préstamos', 'Registra entregas y devoluciones.'],
  sanciones: ['Sanciones', 'Restricciones informadas por el sistema.'],
  administracion: ['Panel administrador', 'Configuración y estado de la integración.'],
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
        <h1>{publica ? 'Catálogo público' : titulo}</h1>
        <p>{publica ? 'Consulta los bienes registrados. Inicia sesión como personal para operar.' : subtitulo}</p>
      </div>
    </header>
  )
}
