import { useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import FormularioInspeccion from '../components/FormularioInspeccion'
import { mensajeDeError } from '../services/api'
import type { CatalogoItem, Ejemplar, NuevaInspeccionDevolucion, InspeccionDevolucion, Prestamo, Usuario } from '../types'
import { nombreUsuario, etiquetaLugar, tituloDeEjemplar, fechaHoraLima } from '../utils/format'

export default function Prestamos({ prestamos, usuarios, catalogo, ejemplares, puedeOperar, puedeDevolver, devolverPrestamo }: {
  prestamos: Prestamo[]; usuarios: Usuario[]; catalogo: CatalogoItem[]; ejemplares: Ejemplar[]; puedeOperar: boolean; puedeDevolver: boolean;
  devolverPrestamo: (id: string, inspeccion: NuevaInspeccionDevolucion) => Promise<InspeccionDevolucion>
}) {
  const navegar = useNavigate(); const location = useLocation(); const [q, setQ] = useState(''); const [seleccionado, setSeleccionado] = useState<Prestamo | null>(null); const [enviando, setEnviando] = useState(false); const [mensaje, setMensaje] = useState<string | null>((location.state as { mensaje?: string } | null)?.mensaje ?? null); const [error, setError] = useState<string | null>(null)
  const [limite, setLimite] = useState(10)
  const filas = useMemo(() => prestamos.filter((p) => { const persona = usuarios.find((u) => u.id === p.prestatarioId); const bien = tituloDeEjemplar(ejemplares, catalogo, p.ejemplarId); return p.estado === 'ACTIVO' && `${persona?.nombreCompleto} ${persona?.email} ${bien}`.toLowerCase().includes(q.toLowerCase()) }).sort((a, b) => b.fechaHoraEntrega.localeCompare(a.fechaHoraEntrega)), [prestamos, usuarios, ejemplares, catalogo, q])
  async function devolver(p: Prestamo, inspeccion: NuevaInspeccionDevolucion) { if (enviando) return; setEnviando(true); setError(null); try { await devolverPrestamo(p.id, inspeccion); setSeleccionado(null); setMensaje('Devolución registrada correctamente.') } catch (e) { setError(mensajeDeError(e)) } finally { setEnviando(false) } }
  const columnas: Columna<Prestamo>[] = [
    { clave: 'persona', titulo: 'PERSONA', render: (p) => <div className="celda-principal"><strong>{nombreUsuario(usuarios, p.prestatarioId)}</strong><small>{usuarios.find((u) => u.id === p.prestatarioId)?.email}</small></div> },
    { clave: 'bien', titulo: 'BIEN', render: (p) => <div className="celda-principal"><strong>{tituloDeEjemplar(ejemplares, catalogo, p.ejemplarId)}</strong><small>{etiquetaLugar(p.lugarUso)}</small></div> },
    { clave: 'entrega', titulo: 'ENTREGA (LIMA)', render: (p) => fechaHoraLima(p.fechaHoraEntrega) },
    { clave: 'vence', titulo: 'DEVOLUCIÓN PREVISTA (LIMA)', render: (p) => <div className="celda-principal"><span>{fechaHoraLima(p.fechaHoraVencimiento)}</span>{p.vencido && <small className="texto-peligro">{p.diasRetraso ? `${p.diasRetraso} días de retraso` : 'Horario de devolución vencido'}</small>}{p.diasRetraso > 7 && <small>Al superar 7 días corresponde registrar una multa manual.</small>}</div> },
    { clave: 'estado', titulo: 'ESTADO', render: (p) => <EtiquetaEstado estado={p.vencido ? 'VENCIDO' : p.estado} /> },
    { clave: 'accion', titulo: '', render: (p) => p.estado === 'ACTIVO' && puedeDevolver ? <button className="boton terciario" disabled={enviando} onClick={() => setSeleccionado(p)}>Recibir devolución</button> : null },
  ]
  return <>
    <section className="catalogo-toolbar"><label className="busqueda-catalogo">Buscar préstamo<input className="buscador" type="search" value={q} onChange={(e) => { setQ(e.target.value); setLimite(10) }} placeholder="Persona, correo o bien" /></label>{puedeOperar && <button className="boton primario" disabled={enviando} onClick={() => navegar('/prestamos/nuevo')}>+ Registrar préstamo</button>}</section>
    {mensaje && <div className="aviso correcto-aviso" role="status">✓ {mensaje}</div>}{error && <div className="aviso error-aviso">{error}</div>}
    {seleccionado && <FormularioInspeccion bien={tituloDeEjemplar(ejemplares, catalogo, seleccionado.ejemplarId)} persona={nombreUsuario(usuarios, seleccionado.prestatarioId)} enviando={enviando} confirmar={(i) => devolver(seleccionado, i)} cancelar={() => setSeleccionado(null)} />}
    <section className="panel"><div className="panel-titulo"><div><h2>Préstamos activos</h2><p>Mostrando {Math.min(limite, filas.length)} de {filas.length}. Las devoluciones e inspecciones cerradas se consultan en Reportes.</p></div></div>
      <Tabla filas={filas.slice(0, limite)} columnas={columnas} vacio="No hay préstamos activos que coincidan" />
      {limite < filas.length && <div className="acciones-fila"><button className="boton secundario" type="button" onClick={() => setLimite((n) => n + 10)}>Ver 10 más</button></div>}
    </section>
  </>
}
