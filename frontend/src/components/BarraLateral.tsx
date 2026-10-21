import type { Pagina, Sesion } from '../types'
import { iniciales } from '../utils/format'

const OPCIONES: Array<{ id: Pagina; icono: string; texto: string }> = [
  { id: 'resumen', icono: '⌂', texto: 'Resumen' },
  { id: 'personas', icono: '♙', texto: 'Personas' },
  { id: 'catalogo', icono: '▦', texto: 'Catálogo' },
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
}

export default function BarraLateral({
  pagina,
  cambiarPagina,
  cerrarSesion,
  sesion,
  soloCatalogo = false,
}: Props) {
  const opciones = soloCatalogo ? OPCIONES.filter((opcion) => opcion.id === 'catalogo') : OPCIONES

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
          <small>{sesion.publica ? 'Catálogo público' : 'Personal bibliotecario'}</small>
        </div>
      </div>
      <button className="cerrar" onClick={cerrarSesion} type="button">
        ↪ Cerrar sesión
      </button>
    </aside>
  )
}
