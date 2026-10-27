import type { Pagina, PermisoOperativo, RolAcceso, Sesion } from '../types'
import Icono from './Icono'
import { iniciales } from '../utils/format'

const OPCIONES: Array<{ id: Pagina; icono: string; texto: string }> = [
  { id: 'resumen', icono: '⌂', texto: 'Resumen' },
  { id: 'personas', icono: '♙', texto: 'Personas' },
  { id: 'mi-cuenta', icono: '♙', texto: 'Mi cuenta' },
  { id: 'catalogo', icono: '▦', texto: 'Catálogo' },
  { id: 'mis-prestamos', icono: '↗', texto: 'Mis préstamos' },
  { id: 'mis-reservas', icono: '☰', texto: 'Mis reservas' },
  { id: 'reservas', icono: '☰', texto: 'Aprobar reservas' },
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
  cuentaValidada?: boolean
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
  cuentaValidada = true,
}: Props) {
  const opciones = soloCatalogo
    ? OPCIONES.filter((opcion) => opcion.id === 'catalogo')
    : OPCIONES.filter((opcion) => {
        if (!cuentaValidada) return opcion.id === 'catalogo' || opcion.id === 'mi-cuenta'
        if (opcion.id === 'mi-cuenta') return !sesion.publica
        if (opcion.id === 'catalogo') return true
        if (opcion.id === 'mis-prestamos')
          return !sesion.publica && !permisos.includes('REGISTRAR_PRESTAMO')
        if (opcion.id === 'mis-reservas')
          return !sesion.publica
        if (opcion.id === 'reservas')
          return permisos.includes('ADMINISTRAR_SISTEMA')
        if (opcion.id === 'resumen') return esPersonal
        if (opcion.id === 'prestamos') return permisos.includes('REGISTRAR_PRESTAMO') || permisos.includes('REGISTRAR_DEVOLUCION')
        if (opcion.id === 'personas')
          return (
            permisos.includes('GESTIONAR_USUARIOS') ||
            permisos.includes('VALIDAR_USUARIO_PROVISIONAL')
          )
        if (opcion.id === 'sanciones') return permisos.includes('GESTIONAR_SANCIONES')
        if (opcion.id === 'administracion') return permisos.includes('ADMINISTRAR_SISTEMA')
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
      <p className="nav-etiqueta">EXPLORAR</p>
      <nav aria-label="Navegación principal">
        {opciones.map((opcion) => (
          <button
            key={opcion.id}
            aria-label={opcion.texto}
            title={opcion.texto}
            aria-current={pagina === opcion.id ? 'page' : undefined}
            className={pagina === opcion.id ? 'activo' : ''}
            onClick={() => cambiarPagina(opcion.id)}
            type="button"
          >
            <span><Icono nombre={opcion.id} /></span>
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
      <button aria-label="Cerrar sesión" title="Cerrar sesión" className="cerrar" onClick={cerrarSesion} type="button">
        <Icono nombre="salir" /> Cerrar sesión
      </button>
    </aside>
  )
}
