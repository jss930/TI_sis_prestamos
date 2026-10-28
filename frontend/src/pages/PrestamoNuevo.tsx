import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, Ejemplar, LugarUso, NuevoPrestamo, Usuario } from '../types'

function local(d: Date) { return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }
const PLAZOS = { ALUMNO: { LIBRO: 7, EQUIPO: 1, OTRO: 3 }, DOCENTE: { LIBRO: 30, EQUIPO: 7, OTRO: 7 }, ADMINISTRATIVO: { LIBRO: 15, EQUIPO: 3, OTRO: 5 } } as const

export default function PrestamoNuevo({ usuarios, catalogo, ejemplares, operadorId, crear }: {
  usuarios: Usuario[]; catalogo: CatalogoItem[]; ejemplares: Ejemplar[]; operadorId: string; crear: (v: NuevoPrestamo) => Promise<void>
}) {
  const navegar = useNavigate(); const ahora = useMemo(() => new Date(), [])
  const [busqueda, setBusqueda] = useState(''); const [prestatarioId, setPrestatario] = useState(''); const [itemId, setItem] = useState(''); const [ejemplarId, setEjemplar] = useState('')
  const [lugar, setLugar] = useState<LugarUso>('EN_CAMPUS'); const [entrega, setEntrega] = useState(local(ahora)); const [vencimiento, setVencimiento] = useState(''); const [error, setError] = useState<string | null>(null); const [enviando, setEnviando] = useState(false)
  const personas = useMemo(() => { const q = busqueda.toLowerCase().trim(); return usuarios.filter((u) => u.estado === 'VALIDADO' && u.id !== operadorId && `${u.nombreCompleto} ${u.email} ${u.codigoInstitucional ?? ''}`.toLowerCase().includes(q)).slice(0, 8) }, [usuarios, operadorId, busqueda])
  const persona = usuarios.find((u) => u.id === prestatarioId); const item = catalogo.find((i) => i.id === itemId)
  const copias = ejemplares.filter((e) => e.catalogoItemId === itemId && e.estadoFisico === 'DISPONIBLE')
  function elegirItem(id: string) { setItem(id); setEjemplar(''); const bien = catalogo.find((i) => i.id === id); if (!persona || !bien) return; const dias = PLAZOS[persona.perfil][bien.tipo]; const fin = new Date(entrega); fin.setDate(fin.getDate() + dias); fin.setHours(20, 0, 0, 0); setVencimiento(local(fin)); if (bien.tipo !== 'LIBRO') setLugar('EN_CAMPUS') }
  async function enviar(e: FormEvent) { e.preventDefault(); setError(null); if (!prestatarioId || !ejemplarId || !vencimiento) { setError('Completa la persona, el bien y el horario.'); return } setEnviando(true); try { await crear({ operadorId, prestatarioId, ejemplarId, lugarUso: lugar, fechaHoraEntrega: new Date(entrega).toISOString(), fechaHoraVencimiento: new Date(vencimiento).toISOString() }); navegar('/prestamos', { replace: true, state: { mensaje: 'Préstamo registrado correctamente.' } }) } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo registrar el préstamo.') } finally { setEnviando(false) } }

  return <><PageHeader volverA="/prestamos" titulo="Registrar préstamo" descripcion="Entrega presencial. Verifica a la persona, la copia y la fecha de devolución." />
    {error && <div className="aviso error-aviso" role="alert">{error}</div>}<form className="panel formulario tarea-form" onSubmit={enviar}>
      <section className="form-seccion"><span className="seccion-numero">1</span><div><h2>Persona</h2><p>Busca por nombre, correo o código institucional.</p></div></section>
      {persona ? <div className="seleccion-card"><div><strong>{persona.nombreCompleto}</strong><span>{persona.email} · {persona.perfil}</span></div><button className="boton terciario" type="button" onClick={() => { setPrestatario(''); setBusqueda('') }}>Cambiar</button></div> : <><label>Buscar persona<input autoFocus type="search" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} placeholder="Ej. Ana, ana@unsa.edu.pe o 20241234" /></label>{busqueda && <div className="combobox-resultados">{personas.map((u) => <button type="button" key={u.id} onClick={() => { setPrestatario(u.id); setBusqueda(u.nombreCompleto) }}><strong>{u.nombreCompleto}</strong><span>{u.email} · {u.perfil}</span></button>)}{!personas.length && <p>No hay cuentas validadas coincidentes.</p>}</div>}</>}
      <section className="form-seccion"><span className="seccion-numero">2</span><div><h2>Bien y copia</h2><p>Solo se muestran copias disponibles.</p></div></section>
      <div className="campos dos"><label>Título<select required value={itemId} onChange={(e) => elegirItem(e.target.value)}><option value="">Seleccionar…</option>{catalogo.filter((i) => i.activo).map((i) => <option key={i.id} value={i.id}>{i.titulo}</option>)}</select></label><label>Ejemplar<select required disabled={!itemId} value={ejemplarId} onChange={(e) => setEjemplar(e.target.value)}><option value="">Seleccionar copia…</option>{copias.map((c) => <option key={c.id} value={c.id}>{c.barcode} · {c.sala}, {c.estante}</option>)}</select></label>
        {item?.tipo === 'LIBRO' && <label>Lugar de uso<select value={lugar} onChange={(e) => setLugar(e.target.value as LugarUso)}><option value="EN_CAMPUS">En campus</option><option value="DOMICILIO">A domicilio</option></select></label>}</div>
      <section className="form-seccion"><span className="seccion-numero">3</span><div><h2>Horario</h2><p>La entrega se completa con la hora actual; confirma cuándo debe devolverse.</p></div></section>
      <div className="campos dos"><label>Fecha y hora de entrega<input type="datetime-local" required value={entrega} max={local(new Date(Date.now() + 5 * 60000))} onChange={(e) => setEntrega(e.target.value)} /></label><label>Fecha y hora de devolución<input type="datetime-local" required value={vencimiento} min={entrega} onChange={(e) => setVencimiento(e.target.value)} /><small>El sistema valida el plazo máximo según perfil y tipo de bien.</small></label></div>
      <div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/prestamos')}>Cancelar</button><button className="boton primario" disabled={enviando}>{enviando ? 'Registrando…' : 'Confirmar préstamo'}</button></div>
    </form></>
}
