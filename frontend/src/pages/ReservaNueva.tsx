import { useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, LugarUso, PerfilPrestatario } from '../types'
import { descripcionItem } from '../utils/format'
import { imagenCatalogo } from '../utils/imagenesCatalogo'
import { descripcionPlazo, finMaximo, HORA, instanteLima, siguienteInicioReserva, validarPlazo, valorLima } from '../utils/plazos'

interface Props {
  itemId?: string
  catalogo: CatalogoItem[]
  perfil: PerfilPrestatario
  reservar: (id: string, lugar: LugarUso, inicio: string, fin: string) => Promise<void>
}

export default function ReservaNueva({ itemId, catalogo, perfil, reservar }: Props) {
  const navegar = useNavigate()
  const item = catalogo.find((actual) => actual.id === itemId)
  const inicioInicial = useMemo(() => siguienteInicioReserva(), [])
  const [paso, setPaso] = useState(1)
  const [datos, setDatos] = useState({
    inicio: inicioInicial,
    fin: valorLima(new Date(instanteLima(inicioInicial).getTime() + HORA)),
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
    const fechaInicio = instanteLima(inicio)
    setDatos({
      ...datos,
      inicio,
      fin: Number.isNaN(fechaInicio.getTime())
        ? datos.fin
        : valorLima(new Date(fechaInicio.getTime() + HORA)),
    })
  }

  function validar() {
    if (!item) return
    validarPlazo(datos.inicio, datos.fin, datos.lugar, perfil, item.tipo)
    if (instanteLima(datos.inicio).getTime() < Date.now() + HORA / 2) throw new Error('El inicio debe tener al menos 30 minutos de anticipación.')
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!item || enviando) return
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
        item.tipo === 'LIBRO' || (item.tipo === 'EQUIPO' && perfil !== 'ALUMNO')
          ? datos.lugar
          : 'EN_CAMPUS',
        instanteLima(datos.inicio).toISOString(),
        instanteLima(datos.fin).toISOString(),
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
            <div className="reglas-inline"><span>✓ 30 min de anticipación</span><span>{descripcionPlazo(datos.lugar, perfil, item.tipo)}</span></div>
            <div className="campos dos">
              <label>Fecha y hora de inicio
                <input type="datetime-local" required step="900" min={siguienteInicioReserva()} value={datos.inicio} onChange={(evento) => cambiarInicio(evento.target.value)} />
              </label>
              <label>Fecha y hora final
                <input type="datetime-local" required step="900"
                  min={datos.inicio ? valorLima(new Date(instanteLima(datos.inicio).getTime() + HORA)) : undefined}
                  max={finMaximo(datos.inicio, datos.lugar, perfil, item.tipo) || undefined}
                  value={datos.fin} onChange={(evento) => setDatos({ ...datos, fin: evento.target.value })} />
              </label>
              {item.tipo === 'LIBRO' || (item.tipo === 'EQUIPO' && perfil !== 'ALUMNO') ? (
                <label>Lugar de uso
                  <select value={datos.lugar} onChange={(evento) => setDatos({ ...datos, lugar: evento.target.value as LugarUso,
                    fin: datos.inicio ? valorLima(new Date(instanteLima(datos.inicio).getTime() + HORA)) : '' })}>
                    <option value="EN_CAMPUS">En campus</option><option value="DOMICILIO">A domicilio</option>
                  </select>
                </label>
              ) : <div className="campo-informativo"><span>Lugar de uso</span><strong>En campus</strong><small>Este bien solo puede usarse dentro del campus para tu perfil.</small></div>}
            </div>
            <div className="acciones-finales"><button className="boton primario" type="submit">Continuar</button></div>
          </>
        ) : (
          <>
            <h2>Revisa tu reserva</h2>
            <dl className="resumen-confirmacion">
              <div><dt>Título</dt><dd>{item.titulo}</dd></div>
              <div><dt>Inicio (Lima)</dt><dd>{datos.inicio.replace('T', ' ')}</dd></div>
              <div><dt>Fin (Lima)</dt><dd>{datos.fin.replace('T', ' ')}</dd></div>
              <div><dt>Uso</dt><dd>{datos.lugar === 'DOMICILIO' ? 'A domicilio' : 'En campus'}</dd></div>
            </dl>
            <div className="aviso">La administración revisará la solicitud y asignará una copia disponible.</div>
            <div className="acciones-finales">
              <button className="boton terciario" type="button" disabled={enviando} onClick={() => setPaso(1)}>Editar horario</button>
              <button className="boton primario" disabled={enviando} type="submit">{enviando ? 'Enviando…' : 'Confirmar reserva'}</button>
            </div>
          </>
        )}
      </form>
    </>
  )
}
