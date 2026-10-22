import { useEffect, useState } from 'react'
import { api, mensajeDeError } from '../services/api'
import type { CategoriaResponsable, CambioResponsable, ResumenResponsables, Usuario } from '../types'
import { etiquetaPerfil, nombreUsuario } from '../utils/format'
import EtiquetaEstado from './EtiquetaEstado'

const CATEGORIAS: Array<{ id: CategoriaResponsable; nombre: string; tecnica: boolean }> = [
  { id: 'BIBLIOTECA', nombre: 'Biblioteca', tecnica: false },
  { id: 'DEPORTES', nombre: 'Deportes', tecnica: false },
  { id: 'TECNOLOGIA', nombre: 'Tecnología', tecnica: true },
  { id: 'COMPUTACION', nombre: 'Computación y periféricos', tecnica: true },
]
const ACCIONES: Record<CambioResponsable['accion'], string> = {
  ASIGNADO: 'Responsable asignado', RETIRADO: 'Responsable retirado',
  CAPACITACION_VERIFICADA: 'Capacitación verificada', CAPACITACION_RETIRADA: 'Capacitación retirada',
}

export default function GestionResponsables({ usuarios }: { usuarios: Usuario[] }) {
  const [datos, setDatos] = useState<ResumenResponsables | null>(null)
  const [seleccionado, setSeleccionado] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [recarga, setRecarga] = useState(0)
  useEffect(() => {
    let vigente = true
    setError(null)
    void api.getResponsables().then((resultado) => { if (vigente) setDatos(resultado) })
      .catch((e: unknown) => { if (vigente) setError(mensajeDeError(e)) })
    return () => { vigente = false }
  }, [recarga])
  const persona = usuarios.find((u) => u.id === seleccionado) ?? usuarios[0]
  const responsabilidad = datos?.responsables.find((r) => r.usuarioId === persona?.id)
  const capacitada = responsabilidad?.capacitacionVerificada ?? false
  const activa = persona?.estado === 'VALIDADO'

  async function guardar(accion: () => Promise<ResumenResponsables>) {
    if (guardando) return
    setGuardando(true)
    setError(null)
    setMensaje(null)
    try {
      setDatos(await accion())
      setMensaje('Cambio guardado y registrado en el historial.')
    } catch (e) { setError(mensajeDeError(e)) }
    finally { setGuardando(false) }
  }

  return <>
    <section className="panel formulario gestion-responsables">
      <h2>Gestión de responsables</h2>
      <p>Asigna categorías a las cuentas validadas. Tecnología y Computación requieren capacitación técnica.</p>
      <p>Estas asignaciones no cambian los permisos operativos de la cuenta, que se gestionan en Personas.</p>
      {error && <div className="aviso error-aviso" role="alert">{error}
        <button type="button" className="enlace" disabled={guardando} onClick={() => setRecarga((v) => v + 1)}>Reintentar carga</button>
      </div>}
      {mensaje && <div className="aviso correcto-aviso" role="status">{mensaje}</div>}
      {!datos && !error && <p>Cargando responsables…</p>}
      <label>Seleccionar persona
        <select value={persona?.id ?? ''} disabled={guardando || !usuarios.length} onChange={(e) => setSeleccionado(e.target.value)}>
          {!usuarios.length && <option value="">No hay personas registradas</option>}
          {usuarios.map((u) => <option key={u.id} value={u.id}>{u.nombreCompleto} — {etiquetaPerfil(u.perfil)}</option>)}
        </select>
      </label>
      {persona && datos && <fieldset disabled={guardando}>
        <legend>{persona.nombreCompleto}</legend>
        <EtiquetaEstado estado={persona.estado} />
        <label className="opcion-permiso">
          <input type="checkbox" checked={capacitada} disabled={!activa && !capacitada}
            onChange={(e) => void guardar(() => api.cambiarCapacitacion(persona.id, e.target.checked))} />
          Capacitación técnica verificada
        </label>
        {capacitada && <p>Al retirar la capacitación también se retiran las asignaciones de Tecnología y Computación.</p>}
        <h3>Categorías asignadas</h3>
        {CATEGORIAS.map((categoria) => {
          const asignada = responsabilidad?.categorias.includes(categoria.id) ?? false
          return <label className="opcion-permiso" key={categoria.id}>
            <input type="checkbox" checked={asignada}
              disabled={!asignada && (!activa || (categoria.tecnica && !capacitada))}
              onChange={(e) => void guardar(() => api.asignarResponsable(persona.id, categoria.id, e.target.checked))} />
            {categoria.nombre}{categoria.tecnica && ' — requiere capacitación'}
          </label>
        })}
        {!activa && <p className="aviso">La cuenta no está validada. Puedes retirar asignaciones existentes, pero no añadir nuevas.</p>}
      </fieldset>}
    </section>
    {datos && <>
      <section className="panel">
        <h2>Resumen de responsables</h2>
        {!datos.responsables.some((r) => r.categorias.length) ? <p>Todavía no hay responsables asignados.</p> :
          <ul>{datos.responsables.flatMap((r) => r.categorias.map((c) => <li key={`${r.usuarioId}-${c}`}>
            {nombreUsuario(usuarios, r.usuarioId)} — {CATEGORIAS.find((categoria) => categoria.id === c)?.nombre}
            {usuarios.find((u) => u.id === r.usuarioId)?.estado !== 'VALIDADO' && ' (cuenta no habilitada)'}
          </li>))}</ul>}
      </section>
      <section className="panel">
        <h2>Historial de asignaciones y capacitación</h2>
        {!datos.historial.length ? <p>Aún no se registraron cambios.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Fecha</th><th>Persona</th><th>Categoría</th><th>Acción</th><th>Realizado por</th></tr></thead>
          <tbody>{datos.historial.map((cambio) => <tr key={cambio.id}>
            <td>{new Date(cambio.fecha).toLocaleString('es-PE')}</td>
            <td>{nombreUsuario(usuarios, cambio.usuarioId)}</td>
            <td>{CATEGORIAS.find((c) => c.id === cambio.categoria)?.nombre ?? 'Capacitación técnica'}</td>
            <td>{ACCIONES[cambio.accion]}</td><td>{nombreUsuario(usuarios, cambio.operadorId)}</td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </>}
  </>
}
