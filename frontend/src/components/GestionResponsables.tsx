import { useEffect, useState } from 'react'
import { api, mensajeDeError } from '../services/api'
import type { CategoriaResponsable, CargoResponsable, CambioResponsable, Responsable, ResumenResponsables, Usuario } from '../types'
import { etiquetaPerfil, nombreUsuario } from '../utils/format'
import EtiquetaEstado from './EtiquetaEstado'

const CATEGORIAS: Array<{ id: CategoriaResponsable; nombre: string; tecnica: boolean }> = [
  { id: 'BIBLIOTECA', nombre: 'Biblioteca', tecnica: false },
  { id: 'DEPORTES', nombre: 'Deportes', tecnica: false },
  { id: 'TECNOLOGIA', nombre: 'Tecnología', tecnica: true },
  { id: 'COMPUTACION', nombre: 'Computación y periféricos', tecnica: true },
]
const ACCIONES: Record<CambioResponsable['accion'], string> = {
  ASIGNADO: 'Responsable asignado', RETIRADO: 'Responsable retirado', CARGO_CAMBIADO: 'Cargo cambiado',
  CAPACITACION_VERIFICADA: 'Capacitación verificada', CAPACITACION_RETIRADA: 'Capacitación retirada',
}

export default function GestionResponsables({ usuarios, personaInicialId }: { usuarios: Usuario[]; personaInicialId?: string | null }) {
  const [datos, setDatos] = useState<ResumenResponsables | null>(null)
  const [borradores, setBorradores] = useState<Record<string, Responsable>>({})
  const [seleccionado, setSeleccionado] = useState(personaInicialId ?? '')
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
  const persona = usuarios.find((u) => u.id === seleccionado)
  const responsabilidad = datos?.responsables.find((r) => r.usuarioId === persona?.id)
  const borrador = persona ? borradores[persona.id] ?? responsabilidad ?? {
    usuarioId: persona.id, capacitacionVerificada: false, categorias: [], cargos: {},
  } : null
  const capacitada = borrador?.capacitacionVerificada ?? false
  const categoriasGuardadas = responsabilidad?.categorias ?? []
  const cambiosPendientes = !!borrador && (capacitada !== (responsabilidad?.capacitacionVerificada ?? false)
    || borrador.categorias.length !== categoriasGuardadas.length
    || borrador.categorias.some((categoria) => !categoriasGuardadas.includes(categoria)
      || borrador.cargos[categoria] !== responsabilidad?.cargos[categoria]))
  const activa = persona?.estado === 'VALIDADO'

  function ocupante(categoria: CategoriaResponsable, cargo: CargoResponsable) {
    return datos?.responsables.find((r) => r.cargos[categoria] === cargo)
  }

  function nombreResponsable(categoria: CategoriaResponsable, cargo: CargoResponsable) {
    const responsable = ocupante(categoria, cargo)
    if (!responsable) return 'Vacante'
    const personaAsignada = usuarios.find((u) => u.id === responsable.usuarioId)
    return `${nombreUsuario(usuarios, responsable.usuarioId)}${personaAsignada?.estado !== 'VALIDADO' ? ' (cuenta no habilitada)' : ''}`
  }

  function editar(cambios: Partial<Responsable>) {
    if (!persona || !borrador || guardando) return
    const siguiente = { ...borrador, ...cambios }
    siguiente.cargos = Object.fromEntries(Object.entries(siguiente.cargos)
      .filter(([categoria]) => siguiente.categorias.includes(categoria as CategoriaResponsable)))
    setBorradores((actuales) => ({ ...actuales, [persona.id]: siguiente }))
    setError(null)
    setMensaje(null)
  }

  function descartar() {
    if (!persona || guardando) return
    setBorradores((actuales) => {
      const siguientes = { ...actuales }
      delete siguientes[persona.id]
      return siguientes
    })
    setError(null)
    setMensaje(null)
  }

  async function guardar() {
    if (guardando || !persona || !borrador || !cambiosPendientes) return
    setGuardando(true)
    setError(null)
    setMensaje(null)
    try {
      setDatos(await api.confirmarResponsables(persona.id, capacitada, borrador.categorias, borrador.cargos))
      setBorradores((actuales) => {
        const siguientes = { ...actuales }
        delete siguientes[persona.id]
        return siguientes
      })
      setSeleccionado('')
      setMensaje(`Cambios de ${persona.nombreCompleto} confirmados. Puedes seleccionar otro responsable.`)
    } catch (e) { setError(mensajeDeError(e)) }
    finally { setGuardando(false) }
  }

  return <>
    <section className="panel formulario gestion-responsables">
      <h2>Gestión de responsables</h2>
      <p>Cada área admite un responsable principal y un suplente. Asigna categorías a las cuentas validadas. Tecnología y Computación requieren capacitación técnica.</p>
      <p>Estas asignaciones habilitan la revisión de solicitudes y las operaciones de préstamo y devolución. Cada responsable atiende las áreas asignadas.</p>
      {error && <div className="aviso error-aviso" role="alert">{error}
        <button type="button" className="enlace" disabled={guardando} onClick={() => setRecarga((v) => v + 1)}>Reintentar carga</button>
      </div>}
      {mensaje && <div className="aviso correcto-aviso" role="status">{mensaje}</div>}
      {!datos && !error && <p>Cargando responsables…</p>}
      <label>Seleccionar persona
        <select value={persona?.id ?? ''} disabled={guardando || !usuarios.length} onChange={(e) => { setSeleccionado(e.target.value); setError(null); setMensaje(null) }}>
          <option value="">{usuarios.length ? 'Seleccionar persona…' : 'No hay personas registradas'}</option>
          {usuarios.map((u) => <option key={u.id} value={u.id}>{u.nombreCompleto} — {etiquetaPerfil(u.perfil)}</option>)}
        </select>
      </label>
      {persona && datos && <fieldset disabled={guardando}>
        <legend>{persona.nombreCompleto}</legend>
        <EtiquetaEstado estado={persona.estado} />
        <label className="opcion-permiso">
          <input type="checkbox" checked={capacitada} disabled={!activa && !capacitada}
            onChange={(e) => editar({ capacitacionVerificada: e.target.checked,
              categorias: e.target.checked ? borrador?.categorias ?? []
                : (borrador?.categorias ?? []).filter((categoria) => !CATEGORIAS.find((c) => c.id === categoria)?.tecnica),
            })} />
          Capacitación técnica verificada
        </label>
        {capacitada && <p>Al retirar la capacitación también se retiran las asignaciones de Tecnología y Computación.</p>}
        <h3>Seleccionar categorías</h3>
        {CATEGORIAS.map((categoria) => {
          const asignada = borrador?.categorias.includes(categoria.id) ?? false
          const principal = ocupante(categoria.id, 'PRINCIPAL')
          const suplente = ocupante(categoria.id, 'SUPLENTE')
          const principalOcupado = !!principal && principal.usuarioId !== persona.id
          const suplenteOcupado = !!suplente && suplente.usuarioId !== persona.id
          const completa = principalOcupado && suplenteOcupado
          return <div key={categoria.id}>
            <label className="opcion-permiso">
              <input type="checkbox" checked={asignada}
                disabled={!asignada && (!activa || (categoria.tecnica && !capacitada) || completa)}
                onChange={(e) => editar({
                  categorias: e.target.checked ? [...(borrador?.categorias ?? []), categoria.id]
                    : (borrador?.categorias ?? []).filter((id) => id !== categoria.id),
                  cargos: { ...borrador?.cargos, [categoria.id]: principalOcupado ? 'SUPLENTE' : 'PRINCIPAL' },
                })} />
              {categoria.nombre}{categoria.tecnica && ' — requiere capacitación'}
            </label>
            {asignada && <label>Cargo en {categoria.nombre}
              <select value={borrador?.cargos[categoria.id] ?? 'PRINCIPAL'} disabled={!activa}
                aria-label={`Cargo en ${categoria.nombre}`}
                onChange={(e) => editar({ cargos: { ...borrador?.cargos, [categoria.id]: e.target.value as CargoResponsable } })}>
                <option value="PRINCIPAL" disabled={principalOcupado}>Principal{principalOcupado ? ' — ocupado' : ''}</option>
                <option value="SUPLENTE" disabled={suplenteOcupado}>Suplente{suplenteOcupado ? ' — ocupado' : ''}</option>
              </select>
            </label>}
            <p>Principal: {nombreResponsable(categoria.id, 'PRINCIPAL')}. Suplente: {nombreResponsable(categoria.id, 'SUPLENTE')}.</p>
            {completa && !asignada && <p>Área completa. Retira o cambia una asignación actual para reemplazarla.</p>}
          </div>
        })}
        {!activa && <p className="aviso">La cuenta no está validada. Puedes retirar asignaciones existentes, pero no añadir nuevas.</p>}
        <p role="status">{cambiosPendientes
          ? 'Tienes cambios pendientes. Revisa la selección y pulsa Confirmar cambios para guardarla.'
          : 'Marca las opciones que deseas cambiar y después confirma la selección.'}</p>
        <div className="acciones">
          <button type="button" className="boton primario" disabled={!cambiosPendientes || guardando}
            onClick={() => void guardar()}>{guardando ? 'Guardando…' : 'Confirmar cambios'}</button>
          <button type="button" className="boton secundario" disabled={!cambiosPendientes || guardando}
            onClick={descartar}>Descartar cambios</button>
        </div>
      </fieldset>}
    </section>
    {datos && <>
      <section className="panel">
        <h2>Resumen de responsables</h2>
        <div className="tabla-contenedor"><table>
          <thead><tr><th>Área</th><th>Principal</th><th>Suplente</th></tr></thead>
          <tbody>{CATEGORIAS.map((categoria) => <tr key={categoria.id}>
            <td>{categoria.nombre}</td>
            <td>{nombreResponsable(categoria.id, 'PRINCIPAL')}</td>
            <td>{nombreResponsable(categoria.id, 'SUPLENTE')}</td>
          </tr>)}</tbody>
        </table></div>
      </section>
      <section className="panel">
        <h2>Historial de asignaciones y capacitación</h2>
        {!datos.historial.length ? <p>Aún no se registraron cambios.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Fecha</th><th>Persona</th><th>Categoría</th><th>Cargo</th><th>Acción</th><th>Realizado por</th></tr></thead>
          <tbody>{datos.historial.map((cambio) => <tr key={cambio.id}>
            <td>{new Date(cambio.fecha).toLocaleString('es-PE')}</td>
            <td>{nombreUsuario(usuarios, cambio.usuarioId)}</td>
            <td>{CATEGORIAS.find((c) => c.id === cambio.categoria)?.nombre ?? 'Capacitación técnica'}</td>
            <td>{cambio.cargo === 'PRINCIPAL' ? 'Principal' : cambio.cargo === 'SUPLENTE' ? 'Suplente' : '—'}</td>
            <td>{ACCIONES[cambio.accion]}</td><td>{nombreUsuario(usuarios, cambio.operadorId)}</td>
          </tr>)}</tbody>
        </table></div>}
      </section>
    </>}
  </>
}
