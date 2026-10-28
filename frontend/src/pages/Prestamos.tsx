import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import FormularioInspeccion from '../components/FormularioInspeccion'
import { api, mensajeDeError } from '../services/api'
import type { CatalogoItem, Ejemplar, NuevaInspeccionDevolucion, InspeccionDevolucion, Prestamo, Usuario } from '../types'
import { nombreUsuario, etiquetaLugar, tituloDeEjemplar } from '../utils/format'

export default function Prestamos({ prestamos, usuarios, catalogo, ejemplares, puedeOperar, puedeDevolver, devolverPrestamo }: {
  prestamos: Prestamo[]; usuarios: Usuario[]; catalogo: CatalogoItem[]; ejemplares: Ejemplar[]; puedeOperar: boolean; puedeDevolver: boolean;
  devolverPrestamo: (id: string, inspeccion: NuevaInspeccionDevolucion) => Promise<InspeccionDevolucion>
}) {
  const navegar = useNavigate(); const location = useLocation(); const [q, setQ] = useState(''); const [seleccionado, setSeleccionado] = useState<Prestamo | null>(null); const [enviando, setEnviando] = useState(false); const [mensaje, setMensaje] = useState<string | null>((location.state as { mensaje?: string } | null)?.mensaje ?? null); const [error, setError] = useState<string | null>(null); const [inspecciones, setInspecciones] = useState<InspeccionDevolucion[]>([])
  useEffect(() => { if (puedeDevolver) void api.getInspeccionesDevolucion().then(setInspecciones).catch(() => undefined) }, [puedeDevolver])
  const filas = useMemo(() => prestamos.filter((p) => { const persona = usuarios.find((u) => u.id === p.prestatarioId); const bien = tituloDeEjemplar(ejemplares, catalogo, p.ejemplarId); return `${persona?.nombreCompleto} ${persona?.email} ${bien}`.toLowerCase().includes(q.toLowerCase()) }).sort((a, b) => b.fechaHoraEntrega.localeCompare(a.fechaHoraEntrega)), [prestamos, usuarios, ejemplares, catalogo, q])
  async function devolver(p: Prestamo, inspeccion: NuevaInspeccionDevolucion) { setEnviando(true); setError(null); try { const r = await devolverPrestamo(p.id, inspeccion); setInspecciones((v) => [r, ...v]); setSeleccionado(null); setMensaje('Devolución registrada correctamente.') } catch (e) { setError(mensajeDeError(e)) } finally { setEnviando(false) } }
  const columnas: Columna<Prestamo>[] = [
    { clave: 'persona', titulo: 'PERSONA', render: (p) => <div className="celda-principal"><strong>{nombreUsuario(usuarios, p.prestatarioId)}</strong><small>{usuarios.find((u) => u.id === p.prestatarioId)?.email}</small></div> },
    { clave: 'bien', titulo: 'BIEN', render: (p) => <div className="celda-principal"><strong>{tituloDeEjemplar(ejemplares, catalogo, p.ejemplarId)}</strong><small>{etiquetaLugar(p.lugarUso)}</small></div> },
    { clave: 'entrega', titulo: 'ENTREGA', render: (p) => new Date(p.fechaHoraEntrega).toLocaleString('es-PE') },
    { clave: 'vence', titulo: 'DEVOLUCIÓN PREVISTA', render: (p) => <div className="celda-principal"><span>{new Date(p.fechaHoraVencimiento).toLocaleString('es-PE')}</span>{p.vencido && <small className="texto-peligro">{p.diasRetraso} días de retraso</small>}</div> },
    { clave: 'estado', titulo: 'ESTADO', render: (p) => <EtiquetaEstado estado={p.vencido ? 'VENCIDO' : p.estado} /> },
    { clave: 'accion', titulo: '', render: (p) => p.estado === 'ACTIVO' && puedeDevolver ? <button className="boton terciario" onClick={() => setSeleccionado(p)}>Recibir devolución</button> : null },
  ]
  return <>
    <section className="catalogo-toolbar"><label className="busqueda-catalogo">Buscar préstamo<input className="buscador" type="search" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Persona, correo o bien" /></label>{puedeOperar && <button className="boton primario" onClick={() => navegar('/prestamos/nuevo')}>+ Registrar préstamo</button>}</section>
    {mensaje && <div className="aviso correcto-aviso" role="status">✓ {mensaje}</div>}{error && <div className="aviso error-aviso">{error}</div>}
    {seleccionado && <FormularioInspeccion bien={tituloDeEjemplar(ejemplares, catalogo, seleccionado.ejemplarId)} persona={nombreUsuario(usuarios, seleccionado.prestatarioId)} enviando={enviando} confirmar={(i) => devolver(seleccionado, i)} cancelar={() => setSeleccionado(null)} />}
    <section className="panel"><div className="panel-titulo"><div><h2>Préstamos</h2><p>{filas.length} registros visibles</p></div></div><Tabla filas={filas} columnas={columnas} vacio="No hay préstamos que coincidan" /></section>
    <details className="panel historial-secundario"><summary>Historial de inspecciones ({inspecciones.length})</summary><div className="tabla-contenedor"><table><thead><tr><th>FECHA</th><th>BIEN</th><th>ESTADO</th><th>OBSERVACIONES</th></tr></thead><tbody>{inspecciones.map((i) => { const p = prestamos.find((x) => x.id === i.prestamoId); return <tr key={i.prestamoId}><td>{new Date(i.fecha).toLocaleString('es-PE')}</td><td>{p ? tituloDeEjemplar(ejemplares, catalogo, p.ejemplarId) : i.prestamoId}</td><td><EtiquetaEstado estado={i.estadoResultante} /></td><td>{i.observaciones || '—'}</td></tr> })}</tbody></table></div></details>
  </>
}
