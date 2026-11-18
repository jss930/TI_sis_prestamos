import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { CatalogoItem, Ejemplar, PerfilPrestatario, Prestamo } from '../types'
import { etiquetaLugar, tituloDeEjemplar } from '../utils/format'
import { finMaximo, HORA, instanteLima, validarPlazo, valorLima } from '../utils/plazos'
import { useAhora } from '../hooks/useAhora'

interface Props {
  prestamos: Prestamo[]
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
  perfil: PerfilPrestatario
  renovar: (id: string, fin: string) => Promise<void>
}

export default function MisPrestamos({ prestamos, catalogo, ejemplares, perfil, renovar }: Props) {
  const ahora = useAhora()
  const [seleccionado, setSeleccionado] = useState<Prestamo | null>(null)
  const [fin, setFin] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)
  function itemDe(p: Prestamo) {
    return catalogo.find((i) => i.id === ejemplares.find((e) => e.id === p.ejemplarId)?.catalogoItemId)
  }
  function maximo(p: Prestamo) {
    const item = itemDe(p)
    return item ? finMaximo(valorLima(new Date(p.fechaHoraEntrega)), p.lugarUso, perfil, item.tipo) : ''
  }
  function puedeRenovar(p: Prestamo) {
    const limite = maximo(p)
    return p.estado === 'ACTIVO' && ahora < Date.parse(p.fechaHoraVencimiento)
      && p.renovaciones < (perfil === 'ALUMNO' ? 1 : 2) && !!limite
      && instanteLima(limite).getTime() > Date.parse(p.fechaHoraVencimiento)
  }
  async function guardar(e: FormEvent) {
    e.preventDefault()
    if (!seleccionado || enviando) return
    setError(null)
    try {
      const item = itemDe(seleccionado)
      if (!item || !puedeRenovar(seleccionado)) throw new Error('Este préstamo no admite renovación.')
      validarPlazo(valorLima(new Date(seleccionado.fechaHoraEntrega)), fin, seleccionado.lugarUso, perfil, item.tipo)
      if (instanteLima(fin).getTime() <= Date.parse(seleccionado.fechaHoraVencimiento)) throw new Error('El nuevo vencimiento debe ampliar el actual.')
      setEnviando(true)
      await renovar(seleccionado.id, instanteLima(fin).toISOString())
      setSeleccionado(null)
      setMensaje('Renovación registrada.')
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo renovar.') }
    finally { setEnviando(false) }
  }
  const ordenados = [...prestamos].sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio))

  const columnas: Columna<Prestamo>[] = [
    {
      clave: 'bien',
      titulo: 'BIEN',
      render: (prestamo) => (
        <div className="celda-principal">
          <strong>{tituloDeEjemplar(ejemplares, catalogo, prestamo.ejemplarId)}</strong>
          <small>{etiquetaLugar(prestamo.lugarUso)}</small>
        </div>
      ),
    },
    {
      clave: 'entrega',
      titulo: 'ENTREGA',
      render: (prestamo) => new Date(prestamo.fechaHoraEntrega).toLocaleString('es-PE', { timeZone: 'America/Lima' }),
    },
    {
      clave: 'vencimiento',
      titulo: 'VENCE',
      render: (prestamo) => (
        <div className="celda-principal">
          <span>{new Date(prestamo.fechaHoraVencimiento).toLocaleString('es-PE', { timeZone: 'America/Lima' })}</span>
          {prestamo.vencido && (
            <small className="texto-peligro">{prestamo.diasRetraso ? `${prestamo.diasRetraso} días de retraso` : 'Horario de devolución vencido'}</small>
          )}
        </div>
      ),
    },
    { clave: 'renovar', titulo: 'ACCIÓN', render: (p) => puedeRenovar(p) ? <button className="boton secundario" disabled={enviando} onClick={() => {
      const limite = instanteLima(maximo(p)).getTime()
      setFin(valorLima(new Date(Math.min(Date.parse(p.fechaHoraVencimiento) + HORA, limite))))
      setSeleccionado(p); setError(null); setMensaje(null)
    }}>Renovar</button> : null },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (prestamo) => (
        <EtiquetaEstado estado={prestamo.vencido ? 'VENCIDO' : prestamo.estado} />
      ),
    },
  ]

  return (
    <section className="panel">
      <div className="panel-titulo">
        <div>
          <h2>Mis préstamos</h2>
          <p>{ordenados.length} registros de tu cuenta</p>
          <Link className="boton secundario" to="/historial">Ver historial y reporte</Link>
        </div>
      </div>
      {error && <div className="aviso error-aviso" role="alert">{error}</div>}
      {mensaje && <div className="aviso correcto-aviso" role="status">{mensaje}</div>}
      {seleccionado && <form className="formulario" onSubmit={guardar}>
        <h3>Renovar préstamo (hora de Lima)</h3><p>La renovación no amplía el máximo total permitido desde la entrega original.</p>
        <label>Nuevo vencimiento<input required type="datetime-local" disabled={enviando} value={fin}
          min={valorLima(new Date(Date.parse(seleccionado.fechaHoraVencimiento) + 60000))}
          max={maximo(seleccionado)} onChange={(e) => setFin(e.target.value)} /></label>
        <div className="acciones-fila"><button className="boton primario" disabled={enviando}>Confirmar renovación</button>
          <button className="boton secundario" type="button" disabled={enviando} onClick={() => setSeleccionado(null)}>Cancelar</button></div>
      </form>}
      <Tabla filas={ordenados} columnas={columnas} vacio="Todavía no tienes préstamos registrados" />
    </section>
  )
}
