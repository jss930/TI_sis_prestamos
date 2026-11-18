import { useEffect, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { CatalogoItem, PerfilPrestatario, Reserva } from '../types'
import { fechaCorta, tituloDeCatalogo } from '../utils/format'
import { descripcionPlazo, HORA, instanteLima, validarPlazo, valorLima } from '../utils/plazos'
import { useAhora } from '../hooks/useAhora'

interface Props {
  reservas: Reserva[]
  catalogo: CatalogoItem[]
  usuarioId: string | null
  perfil: PerfilPrestatario
  cancelarReserva: (id: string) => Promise<void>
  reprogramarReserva: (id: string, inicio: string, fin: string) => Promise<void>
}

export default function MisReservas({ reservas, catalogo, usuarioId, perfil, cancelarReserva, reprogramarReserva }: Props) {
  const ahora = useAhora()
  const location = useLocation()
  const navegar = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>((location.state as { mensaje?: string } | null)?.mensaje ?? null)
  const [procesando, setProcesando] = useState<string | null>(null)
  const [editando, setEditando] = useState<string | null>(null)
  const [horario, setHorario] = useState({ inicio: '', fin: '' })

  useEffect(() => {
    if ((location.state as { mensaje?: string } | null)?.mensaje) {
      navegar(location.pathname, { replace: true, state: null })
    }
  }, [location.pathname, location.state, navegar])

  const mias = reservas
    .filter((r) => r.prestatarioId === usuarioId)
    .sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))

  async function cancelar(id: string) {
    if (procesando) return
    setError(null)
    setExito(null)
    setProcesando(id)
    try {
      await cancelarReserva(id)
      setExito('Reserva cancelada correctamente.')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo cancelar la reserva.')
    } finally {
      setProcesando(null)
    }
  }

  async function guardarHorario(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!editando || procesando) return
    const inicio = instanteLima(horario.inicio)
    const fin = instanteLima(horario.fin)
    const reserva = reservas.find((r) => r.id === editando)
    const item = catalogo.find((i) => i.id === reserva?.catalogoItemId)
    try {
      if (!reserva || !item) throw new Error('No se encontró la reserva.')
      validarPlazo(horario.inicio, horario.fin, reserva.lugarUso, perfil, item.tipo)
      if (inicio.getTime() < Date.now() + HORA / 2) throw new Error('El nuevo inicio debe tener al menos 30 minutos de anticipación.')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Horario inválido.')
      return
    }
    setProcesando(editando)
    setError(null)
    try {
      await reprogramarReserva(editando, inicio.toISOString(), fin.toISOString())
      setExito('Reserva reprogramada.')
      setEditando(null)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo reprogramar.')
    } finally {
      setProcesando(null)
    }
  }

  const columnas: Columna<Reserva>[] = [
    {
      clave: 'item',
      titulo: 'ÍTEM',
      render: (r) => <strong>{tituloDeCatalogo(catalogo, r.catalogoItemId)}</strong>,
    },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (r) => <EtiquetaEstado estado={r.estado} />,
    },
    {
      clave: 'fecha',
      titulo: 'HORARIO RESERVADO',
      render: (r) => r.inicioReserva && r.finReserva
        ? <><strong>{new Date(r.inicioReserva).toLocaleString('es-PE')}</strong><br /><small>hasta {new Date(r.finReserva).toLocaleString('es-PE')}</small></>
        : fechaCorta(r.fechaAlta.slice(0, 10)),
    },
    {
      clave: 'motivo',
      titulo: 'RECOGIDA / MOTIVO',
      render: (r) => r.estado === 'APROBADA' && r.fechaLimiteRetiro
        ? `Recoge antes del ${new Date(r.fechaLimiteRetiro).toLocaleString('es-PE')}`
        : r.motivo ?? (r.estado === 'ATENDIDA' ? 'Entrega registrada; consulta Mis préstamos' : '—'),
    },
    {
      clave: 'accion',
      titulo: 'ACCIÓN',
      render: (r) =>
        (r.estado === 'SOLICITADA' || r.estado === 'APROBADA') ? (<div className="acciones-fila">
          {r.estado === 'SOLICITADA' && r.inicioReserva && ahora < Date.parse(r.inicioReserva) - HORA && <button className="boton secundario" type="button" disabled={!!procesando} onClick={() => {
            setHorario({ inicio: valorLima(new Date(r.inicioReserva!)), fin: valorLima(new Date(r.finReserva!)) }); setEditando(r.id)
          }}>Reprogramar</button>}
          <button
            className="boton secundario"
            disabled={!!procesando}
            onClick={() => void cancelar(r.id)}
            type="button"
          >
            {procesando === r.id ? 'Cancelando…' : 'Cancelar'}
          </button></div>
        ) : null,
    },
  ]

  return (
    <section className="panel">
      <div className="panel-titulo">
        <div>
          <h2>Mis solicitudes</h2>
          <p>{mias.length} solicitudes de tu cuenta</p>
        </div>
      </div>
      {error && <div className="aviso error-aviso">{error}</div>}
      {exito && <div className="aviso correcto-aviso" role="status">✓ {exito}</div>}
      {editando && <form className="formulario" onSubmit={guardarHorario}>
        <h3>Reprogramar reserva (hora de Lima)</h3><p>Disponible hasta una hora antes del inicio. El nuevo horario conserva las reglas de tu perfil y lugar de uso.</p>
        <div className="reserva-horario-grid"><label>Nuevo inicio<input type="datetime-local" required value={horario.inicio} onChange={(e) => setHorario({ ...horario, inicio: e.target.value })} /></label>
        <label>Nuevo fin<input type="datetime-local" required value={horario.fin} onChange={(e) => setHorario({ ...horario, fin: e.target.value })} /></label></div>
        {(() => { const r = reservas.find((r) => r.id === editando); const item = catalogo.find((i) => i.id === r?.catalogoItemId); return r && item ? <p>{descripcionPlazo(r.lugarUso, perfil, item.tipo)}</p> : null })()}
        <div className="acciones-fila"><button className="boton primario" type="submit" disabled={!!procesando}>Guardar horario</button><button className="boton secundario" type="button" disabled={!!procesando} onClick={() => setEditando(null)}>Cancelar</button></div>
      </form>}
      <Tabla filas={mias} columnas={columnas} vacio="Todavía no tienes reservas registradas" />
    </section>
  )
}
