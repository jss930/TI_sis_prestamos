import { useEffect, useState } from 'react'
import type { Pagina, PermisoOperativo, RolAcceso, Sesion } from '../types'
import Icono from './Icono'
import { iniciales } from '../utils/format'
import { opcionesNavegacion } from '../utils/navegacion'

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

export default function BarraLateral({ pagina, cambiarPagina, cerrarSesion, sesion,
  soloCatalogo = false, permisos = [], rolAcceso, cuentaValidada = true }: Props) {
  const enAtencion = pagina === 'reservas' || pagina === 'prestamos'
  const [atencionAbierta, setAtencionAbierta] = useState(enAtencion)
  useEffect(() => { if (enAtencion) setAtencionAbierta(true) }, [enAtencion])
  const opciones = opcionesNavegacion(permisos, cuentaValidada, soloCatalogo)
  const textos: Partial<Record<Pagina, string>> = {
    resumen: 'Resumen', catalogo: 'Catálogo', reservas: 'Reservas', prestamos: 'Préstamos y devoluciones',
    personas: 'Personas', historial: 'Reportes', actividad: 'Mi actividad',
  }
  function opcion(id: Pagina) {
    return <button key={id} aria-label={textos[id]} title={textos[id]} type="button"
      aria-current={pagina === id ? 'page' : undefined} className={pagina === id ? 'activo' : ''}
      onClick={() => cambiarPagina(id)}>
      <span><Icono nombre={id === 'actividad' ? 'mis-prestamos' : id} /></span>{textos[id]}
    </button>
  }
  return <aside className="barra-lateral">
    <div className="marca"><span className="marca-icono">B</span><div><strong>Biblioteca</strong><small>UNSA</small></div></div>
    <p className="nav-etiqueta">EXPLORAR</p>
    <nav aria-label="Navegación principal">
      {opciones.filter((id) => ['resumen', 'catalogo'].includes(id)).map(opcion)}
      {opciones.some((id) => ['reservas', 'prestamos'].includes(id)) && <div className="nav-grupo">
        <button type="button" className="nav-grupo-titulo" aria-expanded={atencionAbierta} aria-controls="nav-atencion"
          onClick={() => setAtencionAbierta((v) => !v)}><span><Icono nombre="prestamos" /></span>Atención <b aria-hidden="true">{atencionAbierta ? '−' : '+'}</b></button>
        <div id="nav-atencion" className="nav-submenu" hidden={!atencionAbierta}>
          {opciones.filter((id) => ['reservas', 'prestamos'].includes(id)).map(opcion)}
        </div>
      </div>}
      {opciones.filter((id) => ['personas', 'historial', 'actividad'].includes(id)).map(opcion)}
    </nav>
    {!soloCatalogo && <button className={`usuario-mini acceso-cuenta${pagina === 'mi-cuenta' ? ' cuenta-activa' : ''}`}
      type="button" aria-label="Mi cuenta" title="Ver mi cuenta" aria-current={pagina === 'mi-cuenta' ? 'page' : undefined}
      onClick={() => cambiarPagina('mi-cuenta')}>
      <span className="avatar">{iniciales(sesion.nombre)}</span>
      <span className="usuario-datos"><strong>{sesion.nombre}</strong><small>{rolAcceso ?? 'Cuenta institucional'}</small></span>
    </button>}
    <button aria-label="Cerrar sesión" title="Cerrar sesión" className="cerrar" onClick={cerrarSesion} type="button"><Icono nombre="salir" /> Cerrar sesión</button>
  </aside>
}
