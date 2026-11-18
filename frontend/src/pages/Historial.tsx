import { useEffect, useRef, useState, type FormEvent } from 'react'
import { flushSync } from 'react-dom'
import { useSearchParams } from 'react-router-dom'
import EtiquetaEstado from '../components/EtiquetaEstado'
import { api, mensajeDeError } from '../services/api'
import type { CatalogoItem, Ejemplar, Historial as DatosHistorial, Usuario } from '../types'
import CabeceraReporte from '../components/CabeceraReporte'
import { unirRegistros } from '../utils/reportes'

const VACIOS = { desde: '', hasta: '', estado: '', busqueda: '' }
const fechaHora = (valor: string | null) => valor
  ? new Date(valor).toLocaleString('es-PE', { timeZone: 'America/Lima' }) : '—'

export default function Historial({ usuarioId, administrador, usuarios, ejemplares, catalogo }: {
  usuarioId: string; administrador: boolean; usuarios: Usuario[]; ejemplares: Ejemplar[]; catalogo: CatalogoItem[]
}) {
  const [parametros, setParametros] = useSearchParams()
  const tipo = administrador ? parametros.get('tipo') === 'ejemplar' ? 'ejemplar'
    : parametros.get('tipo') === 'usuario' ? 'usuario' : 'general' : 'usuario'
  const id = tipo === 'general' ? '' : administrador
    ? parametros.get('id') || (tipo === 'usuario' ? usuarioId : ejemplares[0]?.id ?? '') : usuarioId
  const [filtros, setFiltros] = useState(VACIOS)
  const [aplicados, setAplicados] = useState(VACIOS)
  const [reporte, setReporte] = useState<DatosHistorial | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  const [cargandoMas, setCargandoMas] = useState(false)
  const [imprimiendo, setImprimiendo] = useState(false)
  const [imprimible, setImprimible] = useState<DatosHistorial | null>(null)
  const clave = `${tipo}|${id}|${JSON.stringify(aplicados)}|${revision}`
  const claveActual = useRef(clave)
  claveActual.current = clave

  useEffect(() => {
    let vigente = true
    setReporte(null); setError(null); setCargando(true)
    setImprimible(null); setCargandoMas(false); setImprimiendo(false)
    if (!id && tipo !== 'general') { setCargando(false); return }
    void api.getHistorial(tipo, id, aplicados).then((r) => {
      if (vigente) setReporte(r)
    }).catch((e) => {
      if (vigente) setError(mensajeDeError(e))
    }).finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [tipo, id, aplicados, revision])

  async function verMas() {
    if (!reporte || !reporte.hayMas || cargandoMas || cargando) return
    const consulta = clave
    setCargandoMas(true); setError(null)
    try {
      const siguiente = await api.getHistorial(tipo, id, aplicados, reporte.pagina + 1)
      if (claveActual.current !== consulta) return
      setReporte((previo) => previo ? { ...siguiente,
        prestamos: unirRegistros(previo.prestamos, siguiente.prestamos),
        reservas: unirRegistros(previo.reservas, siguiente.reservas),
        sanciones: unirRegistros(previo.sanciones, siguiente.sanciones),
      } : siguiente)
    } catch (e) { if (claveActual.current === consulta) setError(mensajeDeError(e)) }
    finally { if (claveActual.current === consulta) setCargandoMas(false) }
  }

  async function imprimir() {
    if (!reporte || cargando || imprimiendo) return
    const consulta = clave
    setImprimiendo(true); setError(null)
    try {
      const completo = await api.getHistorial(tipo, id, aplicados, 0, true)
      if (claveActual.current !== consulta) return
      flushSync(() => setImprimible(completo))
      window.print()
    } catch (e) { if (claveActual.current === consulta) setError(mensajeDeError(e)) }
    finally { if (claveActual.current === consulta) setImprimiendo(false) }
  }

  function filtrar(e: FormEvent) {
    e.preventDefault()
    if (filtros.desde && filtros.hasta && filtros.desde > filtros.hasta) {
      setError('La fecha inicial no puede superar la final.'); return
    }
    setAplicados({ ...filtros })
  }

  return <div className="historial-contenedor">
    <form className="panel formulario historial-filtros no-imprimir" onSubmit={filtrar}>
      <h2>{administrador ? 'Consultar reportes' : 'Mi historial'}</h2>
      {administrador && <div className="campos dos">
        <label>Tipo de historial<select value={tipo} onChange={(e) => setParametros(e.target.value === 'general' ? { tipo: 'general' }
          : { tipo: e.target.value, id: e.target.value === 'usuario' ? usuarioId : ejemplares[0]?.id ?? '' })}>
          <option value="general">General</option><option value="usuario">Por usuario</option><option value="ejemplar">Por ejemplar físico</option>
        </select></label>
        {tipo !== 'general' && <label>{tipo === 'usuario' ? 'Persona' : 'Ejemplar'}<select value={id} onChange={(e) => setParametros({ tipo, id: e.target.value })}>
          <option value="">Seleccionar…</option>
          {tipo === 'usuario' ? usuarios.map((u) => <option key={u.id} value={u.id}>{u.nombreCompleto} · {u.codigoInstitucional || u.email}</option>)
            : ejemplares.map((c) => <option key={c.id} value={c.id}>{c.barcode} · {catalogo.find((i) => i.id === c.catalogoItemId)?.titulo}</option>)}
        </select></label>}
      </div>}
      <div className="campos dos">
        <label>Desde<input type="date" value={filtros.desde} max={filtros.hasta || undefined} onChange={(e) => setFiltros({ ...filtros, desde: e.target.value })} /></label>
        <label>Hasta (incluido)<input type="date" value={filtros.hasta} min={filtros.desde || undefined} onChange={(e) => setFiltros({ ...filtros, hasta: e.target.value })} /></label>
        <label>Estado del préstamo<select value={filtros.estado} onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}>
          <option value="">Todos</option><option value="ACTIVO">Activo</option><option value="DEVUELTO">Devuelto</option><option value="PERDIDO">Perdido</option>
        </select></label>
      </div>
      <label>Buscar préstamo por persona o bien<input type="search" maxLength={120} value={filtros.busqueda}
        placeholder="Nombre, correo, código, título o código de ejemplar" onChange={(e) => setFiltros({ ...filtros, busqueda: e.target.value })} /></label>
      <p>Fechas en Lima: entrega para préstamos, inicio para reservas y alta para sanciones. El estado y la búsqueda se aplican solo a préstamos.</p>
      <div className="acciones-fila">
        <button className="boton primario" disabled={cargando || cargandoMas || imprimiendo}>Aplicar filtros</button>
        <button className="boton secundario" type="button" disabled={cargando || imprimiendo} onClick={() => { setFiltros(VACIOS); setAplicados({ ...VACIOS }) }}>Limpiar filtros</button>
        <button className="boton secundario" type="button" disabled={cargando || !reporte || imprimiendo} onClick={() => void imprimir()}>{imprimiendo ? 'Preparando reporte completo…' : 'Imprimir / guardar PDF'}</button>
      </div>
    </form>
    {cargando && <p role="status">Consultando historial…</p>}
    {error && <div className="aviso error-aviso no-imprimir" role="alert">{error}
      <button className="enlace" type="button" disabled={cargando} onClick={() => setRevision((r) => r + 1)}>Reintentar</button>
    </div>}
    {reporte && <div className="no-imprimir">
      <ContenidoHistorial reporte={reporte} />
      <div className="panel acciones-fila">
        <span>Mostrando {reporte.prestamos.length} de {reporte.totalPrestamos} préstamos · {reporte.reservas.length} de {reporte.totalReservas} reservas · {reporte.sanciones.length} de {reporte.totalSanciones} sanciones</span>
        {reporte.hayMas && <button className="boton secundario" type="button" disabled={cargandoMas || imprimiendo} onClick={() => void verMas()}>{cargandoMas ? 'Cargando…' : 'Ver 10 más'}</button>}
      </div>
    </div>}
    {imprimible ? <div className="solo-imprimir"><ContenidoHistorial reporte={imprimible} /></div>
      : <p className="solo-imprimir">Utiliza “Imprimir / guardar PDF” para obtener todos los resultados filtrados.</p>}
  </div>
}

function ContenidoHistorial({ reporte }: { reporte: DatosHistorial }) {
  return <article className="reporte-imprimible">
      <CabeceraReporte titulo={`Reporte histórico ${reporte.usuario ? 'por usuario' : reporte.ejemplar ? 'por ejemplar' : 'general'}`}
        desde={reporte.desde} hasta={reporte.hasta} generadoEn={reporte.generadoEn}>
        {reporte.usuario && <p><strong>{reporte.usuario.nombre}</strong> · Código: {reporte.usuario.codigo || 'No registrado'} · {reporte.usuario.perfil} · {reporte.usuario.estado}</p>}
        {reporte.ejemplar && <p><strong>{reporte.ejemplar.titulo} · {reporte.ejemplar.barcode}</strong> · Estado actual: {reporte.ejemplar.estado}</p>}
        <p>Estado de préstamos: {reporte.estado || 'Todos'}{reporte.busqueda ? ` · Búsqueda: ${reporte.busqueda}` : ''}</p>
      </CabeceraReporte>
      <section className="historial-resumen" aria-label="Indicadores del historial">
        {[
          ['Préstamos', reporte.resumen.totalPrestamos], ['Activos', reporte.resumen.activos],
          ['Vencidos', reporte.resumen.vencidos], ['Devoluciones tardías', reporte.resumen.devolucionesTardias],
          ['Sanciones activas', reporte.resumen.sancionesActivas],
        ].map(([texto, cantidad]) => <div className="panel" key={texto}><strong>{cantidad}</strong><span>{texto}</span></div>)}
      </section>
      <section className="panel">
        <h3>Préstamos ({reporte.totalPrestamos})</h3>
        {!reporte.prestamos.length ? <p>No hay préstamos para estos filtros.</p> : <div className="tabla-contenedor"><table>
          <caption>Entregas, devoluciones e inspecciones. Horas de Lima.</caption>
          <thead><tr><th>Bien / copia</th><th>Prestatario</th><th>Entregó</th><th>Entrega</th><th>Vencimiento</th><th>Devolución</th><th>Resultado</th><th>Inspección</th></tr></thead>
          <tbody>{reporte.prestamos.map((p) => <tr key={p.id}>
            <td>{p.titulo}<small className="tabla-subtexto">{p.barcode} · {p.lugarUso === 'DOMICILIO' ? 'Domicilio' : 'Campus'} · {p.renovaciones} renovaciones</small></td>
            <td>{p.prestatario}</td><td>{p.operador}</td><td>{fechaHora(p.entrega)}</td><td>{fechaHora(p.vencimiento)}</td><td>{fechaHora(p.devolucion)}</td>
            <td><EtiquetaEstado estado={p.vencido ? 'VENCIDO' : p.estado} />{p.devolucion && <small className="tabla-subtexto">{p.devolucionTardia ? 'Devuelto tarde' : 'Devuelto a tiempo'}</small>}</td>
            <td>{p.inspeccion ? <><strong>{p.inspeccion.estadoFisico}</strong><small className="tabla-subtexto">Recibió: {p.inspeccion.recibidoPor} · {fechaHora(p.inspeccion.fecha)}</small>
              <span>{p.inspeccion.accesoriosFaltantes ? 'Accesorios faltantes. ' : ''}{p.inspeccion.observaciones || 'Sin observaciones'}</span></> : 'Sin inspección registrada'}</td>
          </tr>)}</tbody>
        </table></div>}
      </section>
      <section className="panel"><h3>Reservas ({reporte.totalReservas})</h3>
        {reporte.ejemplar && <p>Solo reservas asignadas a esta copia. Las solicitudes por título sin ejemplar no se atribuyen a una unidad física.</p>}
        {!reporte.reservas.length ? <p>No hay reservas para estos filtros.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Persona</th><th>Título</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Motivo</th></tr></thead>
          <tbody>{reporte.reservas.map((r) => <tr key={r.id}><td>{r.prestatario}</td><td>{r.titulo}</td><td>{fechaHora(r.inicio)}</td><td>{fechaHora(r.fin)}</td><td><EtiquetaEstado estado={r.estado} /></td><td>{r.motivo || '—'}</td></tr>)}</tbody>
        </table></div>}
      </section>
      <section className="panel"><h3>Sanciones ({reporte.totalSanciones})</h3>
        {!reporte.sanciones.length ? <p>No hay sanciones para estos filtros.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Persona</th><th>Motivo</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Multa</th><th>Observaciones</th></tr></thead>
          <tbody>{reporte.sanciones.map((s) => <tr key={s.id}><td>{s.persona}</td><td>{s.motivo}</td><td>{s.inicio}</td><td>{s.fin}</td><td>{s.estado}</td><td>S/ {s.multa.toFixed(2)}</td><td>{s.observaciones || '—'}</td></tr>)}</tbody>
        </table></div>}
      </section>
      <footer><small>Reporte de operaciones registradas; no es una auditoría de cada cambio. Los datos históricos sin inspección se muestran como no registrados.</small></footer>
    </article>
}
