import { useEffect, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { CatalogoItem, Reserva } from '../types'
import { fechaCorta, tituloDeCatalogo } from '../utils/format'

interface Props {
  reservas: Reserva[]
  catalogo: CatalogoItem[]
  usuarioId: string | null
  cancelarReserva: (id: string) => Promise<void>
  reprogramarReserva: (id: string, inicio: string, fin: string) => Promise<void>
}

export default function MisReservas({ reservas, catalogo, usuarioId, cancelarReserva, reprogramarReserva }: Props) {
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
    if (!editando) return
    const inicio = new Date(horario.inicio)
    const fin = new Date(horario.fin)
    const reserva = reservas.find((r) => r.id === editando)
    if (inicio.getTime() < Date.now() + 30 * 60 * 1000) {
      setError('El nuevo inicio debe tener al menos 30 minutos de anticipación.')
      return
    }
    if (fin.getTime() - inicio.getTime() < 60 * 60 * 1000) {
      setError('La reserva debe durar al menos una hora.')
      return
    }
    if (fin.getTime() - inicio.getTime() > 24 * 60 * 60 * 1000) {
      setError('La reserva no puede durar más de 24 horas.')
      return
    }
    if (reserva?.lugarUso === 'EN_CAMPUS' && inicio.toDateString() !== fin.toDateString()) {
      setError('Las reservas en campus deben iniciar y terminar el mismo día.')
      return
    }
    for (const fecha of [inicio, fin]) {
      const minutos = fecha.getHours() * 60 + fecha.getMinutes()
      if (minutos < 7 * 60 || minutos > 20 * 60) {
        setError('El horario permitido es de 07:00 a 20:00.')
        return
      }
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
          {r.estado === 'SOLICITADA' && r.inicioReserva && Date.now() < Date.parse(r.inicioReserva) - 60 * 60 * 1000 && <button className="boton secundario" type="button" onClick={() => {
            const local = (iso: string) => { const d = new Date(iso); return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }
            setHorario({ inicio: local(r.inicioReserva!), fin: local(r.finReserva!) }); setEditando(r.id)
          }}>Reprogramar</button>}
          <button
            className="boton secundario"
            disabled={procesando === r.id}
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
        <h3>Reprogramar reserva</h3><p>Disponible hasta una hora antes del inicio. El nuevo horario debe tener al menos 30 minutos de anticipación y durar entre una y 24 horas.</p>
        <div className="reserva-horario-grid"><label>Nuevo inicio<input type="datetime-local" required value={horario.inicio} onChange={(e) => setHorario({ ...horario, inicio: e.target.value })} /></label>
        <label>Nuevo fin<input type="datetime-local" required value={horario.fin} onChange={(e) => setHorario({ ...horario, fin: e.target.value })} /></label></div>
        <div className="acciones-fila"><button className="boton primario" type="submit">Guardar horario</button><button className="boton secundario" type="button" onClick={() => setEditando(null)}>Cancelar</button></div>
      </form>}
      <Tabla filas={mias} columnas={columnas} vacio="Todavía no tienes reservas registradas" />
    </section>
  )
}
