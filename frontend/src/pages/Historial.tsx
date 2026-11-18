import { useEffect, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import EtiquetaEstado from '../components/EtiquetaEstado'
import { api, mensajeDeError } from '../services/api'
import type { CatalogoItem, Ejemplar, Historial as DatosHistorial, Usuario } from '../types'

const VACIOS = { desde: '', hasta: '', estado: '' }
const fechaHora = (valor: string | null) => valor
  ? new Date(valor).toLocaleString('es-PE', { timeZone: 'America/Lima' }) : '—'

export default function Historial({ usuarioId, administrador, usuarios, ejemplares, catalogo }: {
  usuarioId: string; administrador: boolean; usuarios: Usuario[]; ejemplares: Ejemplar[]; catalogo: CatalogoItem[]
}) {
  const [parametros, setParametros] = useSearchParams()
  const tipo = administrador && parametros.get('tipo') === 'ejemplar' ? 'ejemplar' : 'usuario'
  const id = administrador ? parametros.get('id') || usuarioId : usuarioId
  const [filtros, setFiltros] = useState(VACIOS)
  const [aplicados, setAplicados] = useState(VACIOS)
  const [reporte, setReporte] = useState<DatosHistorial | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let vigente = true
    setReporte(null); setError(null); setCargando(true)
    if (!id) { setCargando(false); return }
    void api.getHistorial(tipo, id, aplicados).then((r) => {
      if (vigente) setReporte(r)
    }).catch((e) => {
      if (vigente) setError(mensajeDeError(e))
    }).finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [tipo, id, aplicados, revision])

  function filtrar(e: FormEvent) {
    e.preventDefault()
    if (filtros.desde && filtros.hasta && filtros.desde > filtros.hasta) {
      setError('La fecha inicial no puede superar la final.'); return
    }
    setAplicados({ ...filtros })
  }

  return <div className="historial-contenedor">
    <form className="panel formulario historial-filtros no-imprimir" onSubmit={filtrar}>
      <h2>{administrador ? 'Consultar historial' : 'Mi historial'}</h2>
      {administrador && <div className="campos dos">
        <label>Tipo de historial<select value={tipo} onChange={(e) => setParametros({ tipo: e.target.value, id: e.target.value === 'usuario' ? usuarioId : ejemplares[0]?.id ?? '' })}>
          <option value="usuario">Por usuario</option><option value="ejemplar">Por ejemplar físico</option>
        </select></label>
        <label>{tipo === 'usuario' ? 'Persona' : 'Ejemplar'}<select value={id} onChange={(e) => setParametros({ tipo, id: e.target.value })}>
          <option value="">Seleccionar…</option>
          {tipo === 'usuario' ? usuarios.map((u) => <option key={u.id} value={u.id}>{u.nombreCompleto} · {u.codigoInstitucional || u.email}</option>)
            : ejemplares.map((c) => <option key={c.id} value={c.id}>{c.barcode} · {catalogo.find((i) => i.id === c.catalogoItemId)?.titulo}</option>)}
        </select></label>
      </div>}
      <div className="campos dos">
        <label>Desde<input type="date" value={filtros.desde} max={filtros.hasta || undefined} onChange={(e) => setFiltros({ ...filtros, desde: e.target.value })} /></label>
        <label>Hasta (incluido)<input type="date" value={filtros.hasta} min={filtros.desde || undefined} onChange={(e) => setFiltros({ ...filtros, hasta: e.target.value })} /></label>
        <label>Estado del préstamo<select value={filtros.estado} onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}>
          <option value="">Todos</option><option value="ACTIVO">Activo</option><option value="DEVUELTO">Devuelto</option><option value="PERDIDO">Perdido</option>
        </select></label>
      </div>
      <p>Fechas en Lima: entrega para préstamos, inicio para reservas y alta para sanciones. El filtro de estado se aplica solo a préstamos.</p>
      <div className="acciones-fila">
        <button className="boton primario" disabled={cargando}>Aplicar filtros</button>
        <button className="boton secundario" type="button" disabled={cargando} onClick={() => { setFiltros(VACIOS); setAplicados({ ...VACIOS }) }}>Limpiar filtros</button>
        <button className="boton secundario" type="button" disabled={cargando || !reporte} onClick={() => window.print()}>Imprimir / guardar PDF</button>
      </div>
    </form>
    {cargando && <p role="status">Consultando historial…</p>}
    {error && <div className="aviso error-aviso no-imprimir" role="alert">{error}
      <button className="enlace" type="button" disabled={cargando} onClick={() => setRevision((r) => r + 1)}>Reintentar</button>
    </div>}
    {reporte && <article className="reporte-imprimible">
      <header className="panel">
        <p className="sobrelinea">UNSA · Sistema de Préstamos Universitario</p>
        <h2>Reporte histórico {reporte.usuario ? 'por usuario' : 'por ejemplar'}</h2>
        {reporte.usuario && <p><strong>{reporte.usuario.nombre}</strong> · Código: {reporte.usuario.codigo || 'No registrado'} · {reporte.usuario.perfil} · {reporte.usuario.estado}</p>}
        {reporte.ejemplar && <p><strong>{reporte.ejemplar.titulo} · {reporte.ejemplar.barcode}</strong> · Estado actual: {reporte.ejemplar.estado}</p>}
        <p>Período: {reporte.desde || 'Desde el inicio'} — {reporte.hasta || 'Sin límite final'} · Estado de préstamos: {reporte.estado || 'Todos'}</p>
        <small>Generado: {fechaHora(reporte.generadoEn)} (Lima). Los indicadores corresponden a los registros filtrados.</small>
      </header>
      <section className="historial-resumen" aria-label="Indicadores del historial">
        {[
          ['Préstamos', reporte.resumen.totalPrestamos], ['Activos', reporte.resumen.activos],
          ['Vencidos', reporte.resumen.vencidos], ['Devoluciones tardías', reporte.resumen.devolucionesTardias],
          ['Sanciones activas', reporte.resumen.sancionesActivas],
        ].map(([texto, cantidad]) => <div className="panel" key={texto}><strong>{cantidad}</strong><span>{texto}</span></div>)}
      </section>
      <section className="panel">
        <h3>Préstamos ({reporte.prestamos.length})</h3>
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
      <section className="panel"><h3>Reservas ({reporte.reservas.length})</h3>
        {reporte.ejemplar && <p>Solo reservas asignadas a esta copia. Las solicitudes por título sin ejemplar no se atribuyen a una unidad física.</p>}
        {!reporte.reservas.length ? <p>No hay reservas para estos filtros.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Título</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Motivo</th></tr></thead>
          <tbody>{reporte.reservas.map((r) => <tr key={r.id}><td>{r.titulo}</td><td>{fechaHora(r.inicio)}</td><td>{fechaHora(r.fin)}</td><td><EtiquetaEstado estado={r.estado} /></td><td>{r.motivo || '—'}</td></tr>)}</tbody>
        </table></div>}
      </section>
      <section className="panel"><h3>Sanciones ({reporte.sanciones.length})</h3>
        {!reporte.sanciones.length ? <p>No hay sanciones para estos filtros.</p> : <div className="tabla-contenedor"><table>
          <thead><tr><th>Motivo</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Multa</th><th>Observaciones</th></tr></thead>
          <tbody>{reporte.sanciones.map((s) => <tr key={s.id}><td>{s.motivo}</td><td>{s.inicio}</td><td>{s.fin}</td><td>{s.estado}</td><td>S/ {s.multa.toFixed(2)}</td><td>{s.observaciones || '—'}</td></tr>)}</tbody>
        </table></div>}
      </section>
      <footer><small>Reporte de operaciones registradas; no es una auditoría de cada cambio. Los datos históricos sin inspección se muestran como no registrados.</small></footer>
    </article>}
  </div>
}
