import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, LugarUso } from '../types'
import { descripcionItem } from '../utils/format'
import { imagenCatalogo } from '../utils/imagenesCatalogo'

function valorLocal(fecha: Date) {
  return new Date(fecha.getTime() - fecha.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
}

function siguienteCuartoDeHora() {
  const fecha = new Date(Date.now() + 2 * 60 * 60 * 1000)
  fecha.setMinutes(Math.ceil(fecha.getMinutes() / 15) * 15, 0, 0)
  if (fecha.getHours() < 7) fecha.setHours(7, 0, 0, 0)
  if (fecha.getHours() > 19 || (fecha.getHours() === 19 && fecha.getMinutes() > 0)) {
    fecha.setDate(fecha.getDate() + 1)
    fecha.setHours(7, 0, 0, 0)
  }
  return fecha
}

interface Props {
  itemId?: string
  catalogo: CatalogoItem[]
  reservar: (id: string, lugar: LugarUso, inicio: string, fin: string) => Promise<void>
}

export default function ReservaNueva({ itemId, catalogo, reservar }: Props) {
  const navegar = useNavigate()
  const item = catalogo.find((actual) => actual.id === itemId)
  const inicioInicial = useMemo(siguienteCuartoDeHora, [])
  const [paso, setPaso] = useState(1)
  const [datos, setDatos] = useState({
    inicio: valorLocal(inicioInicial),
    fin: valorLocal(new Date(inicioInicial.getTime() + 60 * 60 * 1000)),
    lugar: 'EN_CAMPUS' as LugarUso,
  })
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (!itemId || !item) {
    return (
      <div className="empty-state pagina-no-encontrada">
        <strong>{itemId ? 'Título no disponible' : 'Enlace de reserva inválido'}</strong>
        <p>Vuelve al catálogo y selecciona nuevamente el recurso que quieres reservar.</p>
        <button className="boton secundario" onClick={() => navegar('/catalogo', { replace: true })} type="button">
          ← Volver al catálogo
        </button>
      </div>
    )
  }

  function cambiarInicio(inicio: string) {
    const fechaInicio = new Date(inicio)
    setDatos({
      ...datos,
      inicio,
      fin: Number.isNaN(fechaInicio.getTime())
        ? datos.fin
        : valorLocal(new Date(fechaInicio.getTime() + 60 * 60 * 1000)),
    })
  }

  function validar() {
    const inicio = new Date(datos.inicio)
    const fin = new Date(datos.fin)
    if (Number.isNaN(inicio.getTime()) || Number.isNaN(fin.getTime())) throw new Error('Selecciona un horario válido.')
    if (inicio.getTime() < Date.now() + 2 * 60 * 60 * 1000) throw new Error('El inicio debe tener al menos 2 horas de anticipación.')
    if (fin <= inicio) throw new Error('La hora final debe ser posterior al inicio.')
    if (fin.getTime() - inicio.getTime() > 24 * 60 * 60 * 1000) throw new Error('La reserva no puede durar más de 24 horas.')
    for (const fecha of [inicio, fin]) {
      const minutos = fecha.getHours() * 60 + fecha.getMinutes()
      if (minutos < 7 * 60 || minutos > 20 * 60) throw new Error('El horario permitido es de 07:00 a 20:00.')
    }
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!item) return
    setError(null)
    try {
      validar()
      if (paso === 1) {
        setPaso(2)
        return
      }
      setEnviando(true)
      await reservar(
        item.id,
        item.tipo === 'LIBRO' ? datos.lugar : 'EN_CAMPUS',
        new Date(datos.inicio).toISOString(),
        new Date(datos.fin).toISOString(),
      )
      navegar('/mis-reservas', { replace: true, state: { mensaje: 'Reserva enviada correctamente.' } })
    } catch (excepcion) {
      setError(excepcion instanceof Error ? excepcion.message : 'No se pudo crear la reserva.')
    } finally {
      setEnviando(false)
    }
  }

  const detalles = [
    item.detalle.autor && ['Autor', item.detalle.autor],
    item.detalle.editorial && ['Editorial', item.detalle.editorial],
    item.detalle.isbn && ['ISBN', item.detalle.isbn],
    item.detalle.anioPublicacion && ['Año', String(item.detalle.anioPublicacion)],
    item.detalle.marca && ['Marca', item.detalle.marca],
    item.detalle.modelo && ['Modelo', item.detalle.modelo],
  ].filter(Boolean) as string[][]

  return (
    <>
      <PageHeader volverA="/catalogo" titulo={`Reservar ${item.titulo}`} descripcion="Revisa el recurso, selecciona el horario y confirma los datos." />
      <ol className="stepper" aria-label="Progreso">
        <li className={paso >= 1 ? 'activo' : ''}>1 <span>Horario</span></li>
        <li className={paso >= 2 ? 'activo' : ''}>2 <span>Confirmar</span></li>
      </ol>
      {error && <div className="aviso error-aviso" role="alert">{error}</div>}
      <form className="panel formulario tarea-form" onSubmit={enviar}>
        {paso === 1 ? (
          <>
            <article className="reserva-item-detalle">
              <img src={imagenCatalogo(item)} alt="" />
              <div>
                <span className="item-tipo">{item.tipo}</span>
                <h2>{item.titulo}</h2>
                <p>{descripcionItem(item)}</p>
                {item.detalle.descripcion && <p>{item.detalle.descripcion}</p>}
                {detalles.length > 0 && <dl>{detalles.map(([etiqueta, valor]) => <div key={etiqueta}><dt>{etiqueta}</dt><dd>{valor}</dd></div>)}</dl>}
              </div>
            </article>
            <div className="reglas-inline"><span>✓ 2 h de anticipación</span><span>✓ 07:00–20:00</span><span>✓ Máximo 24 h</span></div>
            <div className="campos dos">
              <label>Fecha y hora de inicio
                <input type="datetime-local" required step="900" min={valorLocal(siguienteCuartoDeHora())} value={datos.inicio} onChange={(evento) => cambiarInicio(evento.target.value)} />
              </label>
              <label>Fecha y hora final
                <input type="datetime-local" required step="900" min={datos.inicio} value={datos.fin} onChange={(evento) => setDatos({ ...datos, fin: evento.target.value })} />
              </label>
              {item.tipo === 'LIBRO' ? (
                <label>Lugar de uso
                  <select value={datos.lugar} onChange={(evento) => setDatos({ ...datos, lugar: evento.target.value as LugarUso })}>
                    <option value="EN_CAMPUS">En campus</option><option value="DOMICILIO">A domicilio</option>
                  </select>
                </label>
              ) : <div className="campo-informativo"><span>Lugar de uso</span><strong>En campus</strong><small>Este tipo de bien no sale del campus.</small></div>}
            </div>
            <div className="acciones-finales"><button className="boton primario" type="submit">Continuar</button></div>
          </>
        ) : (
          <>
            <h2>Revisa tu reserva</h2>
            <dl className="resumen-confirmacion">
              <div><dt>Título</dt><dd>{item.titulo}</dd></div>
              <div><dt>Inicio</dt><dd>{new Date(datos.inicio).toLocaleString('es-PE')}</dd></div>
              <div><dt>Fin</dt><dd>{new Date(datos.fin).toLocaleString('es-PE')}</dd></div>
              <div><dt>Uso</dt><dd>{datos.lugar === 'DOMICILIO' ? 'A domicilio' : 'En campus'}</dd></div>
            </dl>
            <div className="aviso">La administración revisará la solicitud y asignará una copia disponible.</div>
            <div className="acciones-finales">
              <button className="boton terciario" type="button" onClick={() => setPaso(1)}>Editar horario</button>
              <button className="boton primario" disabled={enviando} type="submit">{enviando ? 'Enviando…' : 'Confirmar reserva'}</button>
            </div>
          </>
        )}
      </form>
    </>
  )
}
