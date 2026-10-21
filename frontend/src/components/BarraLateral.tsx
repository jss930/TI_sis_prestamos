import type { Pagina, PermisoOperativo, RolAcceso, Sesion } from '../types'
import { iniciales } from '../utils/format'

const OPCIONES: Array<{ id: Pagina; icono: string; texto: string }> = [
  { id: 'resumen', icono: '⌂', texto: 'Resumen' },
  { id: 'personas', icono: '♙', texto: 'Personas' },
  { id: 'catalogo', icono: '▦', texto: 'Catálogo' },
  { id: 'mis-prestamos', icono: '↗', texto: 'Mis préstamos' },
  { id: 'prestamos', icono: '↗', texto: 'Préstamos' },
  { id: 'sanciones', icono: '!', texto: 'Sanciones' },
  { id: 'administracion', icono: '⚙', texto: 'Administración' },
]

interface Props {
  pagina: Pagina
  cambiarPagina: (pagina: Pagina) => void
  cerrarSesion: () => void
  sesion: Sesion
  soloCatalogo?: boolean
  permisos?: PermisoOperativo[]
  rolAcceso?: RolAcceso
  esPersonal?: boolean
}

export default function BarraLateral({
  pagina,
  cambiarPagina,
  cerrarSesion,
  sesion,
  soloCatalogo = false,
  permisos = [],
  rolAcceso,
  esPersonal = false,
}: Props) {
  const opciones = soloCatalogo
    ? OPCIONES.filter((opcion) => opcion.id === 'catalogo')
    : OPCIONES.filter((opcion) => {
        if (opcion.id === 'catalogo') return true
        if (opcion.id === 'mis-prestamos')
          return !sesion.publica && !permisos.includes('REGISTRAR_PRESTAMO')
        if (opcion.id === 'resumen') return esPersonal
        if (opcion.id === 'prestamos') return permisos.includes('REGISTRAR_PRESTAMO')
        if (opcion.id === 'personas')
          return (
            permisos.includes('GESTIONAR_USUARIOS') ||
            permisos.includes('VALIDAR_USUARIO_PROVISIONAL')
          )
        if (opcion.id === 'sanciones') return permisos.includes('GESTIONAR_SANCIONES')
        if (opcion.id === 'administracion') return rolAcceso === 'ADMINISTRADOR'
        return false
      })

  const etiquetaRol = sesion.publica
    ? 'Catálogo público'
    : rolAcceso ?? 'Sin permisos operativos'

  return (
    <aside className="barra-lateral">
      <div className="marca">
        <span className="marca-icono">B</span>
        <div>
          <strong>Biblioteca</strong>
          <small>UNSA</small>
        </div>
      </div>
      <nav>
        {opciones.map((opcion) => (
          <button
            key={opcion.id}
            className={pagina === opcion.id ? 'activo' : ''}
            onClick={() => cambiarPagina(opcion.id)}
            type="button"
          >
            <span>{opcion.icono}</span>
            {opcion.texto}
          </button>
        ))}
      </nav>
      <div className="usuario-mini">
        <div className="avatar">{iniciales(sesion.nombre)}</div>
        <div>
          <strong>{sesion.nombre}</strong>
          <small>{etiquetaRol}</small>
        </div>
      </div>
      <button className="cerrar" onClick={cerrarSesion} type="button">
        ↪ Cerrar sesión
      </button>
    </aside>
  )
}