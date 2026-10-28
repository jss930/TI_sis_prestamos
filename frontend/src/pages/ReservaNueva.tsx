import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, LugarUso } from '../types'
import { descripcionItem } from '../utils/format'

function local(fecha: Date) { return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }

export default function ReservaNueva({ catalogo, reservar }: { catalogo: CatalogoItem[];
  reservar: (id: string, lugar: LugarUso, inicio: string, fin: string) => Promise<void> }) {
  const { id } = useParams(); const navegar = useNavigate()
  const item = useMemo(() => catalogo.find((i) => i.id === id), [catalogo, id])
  const inicial = useMemo(() => { const d = new Date(Date.now() + 2 * 3600000); d.setMinutes(Math.ceil(d.getMinutes() / 15) * 15, 0, 0); return d }, [])
  const [paso, setPaso] = useState(1)
  const [datos, setDatos] = useState({ inicio: local(inicial), fin: local(new Date(inicial.getTime() + 3600000)), lugar: 'EN_CAMPUS' as LugarUso })
  const [error, setError] = useState<string | null>(null); const [enviando, setEnviando] = useState(false)
  if (!item) return <div className="empty-state"><strong>Título no encontrado</strong><button className="boton secundario" onClick={() => navegar('/catalogo')}>Volver al catálogo</button></div>

  function validar() {
    const inicio = new Date(datos.inicio), fin = new Date(datos.fin)
    if (inicio.getTime() < Date.now() + 2 * 3600000) throw new Error('El inicio debe tener al menos 2 horas de anticipación.')
    if (fin <= inicio) throw new Error('La hora final debe ser posterior al inicio.')
    if (fin.getTime() - inicio.getTime() > 24 * 3600000) throw new Error('La reserva no puede durar más de 24 horas.')
    for (const fecha of [inicio, fin]) if (fecha.getHours() < 7 || fecha.getHours() > 20) throw new Error('El horario permitido es de 07:00 a 20:00.')
  }
  async function enviar(e: FormEvent) { e.preventDefault(); setError(null); try {
    if (paso === 1) { validar(); setPaso(2); return }
    setEnviando(true); await reservar(item!.id, item!.tipo === 'LIBRO' ? datos.lugar : 'EN_CAMPUS', new Date(datos.inicio).toISOString(), new Date(datos.fin).toISOString())
    navegar('/mis-reservas', { replace: true, state: { mensaje: 'Reserva enviada correctamente.' } })
  } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo crear la reserva.') } finally { setEnviando(false) } }

  return <>
    <PageHeader volverA="/catalogo" titulo={`Reservar ${item.titulo}`} descripcion="Selecciona el horario y confirma los datos." />
    <ol className="stepper" aria-label="Progreso"><li className={paso >= 1 ? 'activo' : ''}>1 <span>Horario</span></li><li className={paso >= 2 ? 'activo' : ''}>2 <span>Confirmar</span></li></ol>
    {error && <div className="aviso error-aviso" role="alert">{error}</div>}
    <form className="panel formulario tarea-form" onSubmit={enviar}>
      {paso === 1 ? <>
        <div className="context-card"><strong>{item.titulo}</strong><span>{descripcionItem(item)}</span></div>
        <div className="reglas-inline"><span>✓ 2 h de anticipación</span><span>✓ 07:00–20:00</span><span>✓ Máximo 24 h</span></div>
        <div className="campos dos"><label>Fecha y hora de inicio<input type="datetime-local" required min={local(new Date(Date.now() + 2 * 3600000))} value={datos.inicio} onChange={(e) => setDatos({ ...datos, inicio: e.target.value })} /></label>
          <label>Fecha y hora final<input type="datetime-local" required value={datos.fin} onChange={(e) => setDatos({ ...datos, fin: e.target.value })} /></label>
          {item.tipo === 'LIBRO' ? <label>Lugar de uso<select value={datos.lugar} onChange={(e) => setDatos({ ...datos, lugar: e.target.value as LugarUso })}><option value="EN_CAMPUS">En campus</option><option value="DOMICILIO">A domicilio</option></select></label>
            : <div className="campo-informativo"><span>Lugar de uso</span><strong>En campus</strong><small>Este tipo de bien no sale del campus.</small></div>}
        </div><div className="acciones-finales"><button className="boton primario" type="submit">Continuar</button></div>
      </> : <>
        <h2>Revisa tu reserva</h2><dl className="resumen-confirmacion"><div><dt>Título</dt><dd>{item.titulo}</dd></div><div><dt>Inicio</dt><dd>{new Date(datos.inicio).toLocaleString('es-PE')}</dd></div><div><dt>Fin</dt><dd>{new Date(datos.fin).toLocaleString('es-PE')}</dd></div><div><dt>Uso</dt><dd>{datos.lugar === 'DOMICILIO' ? 'A domicilio' : 'En campus'}</dd></div></dl>
        <div className="aviso">La administración revisará la solicitud y asignará una copia disponible.</div>
        <div className="acciones-finales"><button className="boton terciario" type="button" onClick={() => setPaso(1)}>Editar horario</button><button className="boton primario" disabled={enviando} type="submit">{enviando ? 'Enviando…' : 'Confirmar reserva'}</button></div>
      </>}
    </form>
  </>
}
