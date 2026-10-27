import { useState } from 'react'
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
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState<string | null>(null)
  const [editando, setEditando] = useState<string | null>(null)
  const [horario, setHorario] = useState({ inicio: '', fin: '' })

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
      {exito && <div className="aviso">{exito}</div>}
      {editando && <form className="formulario" onSubmit={(e) => { e.preventDefault(); setProcesando(editando); setError(null); void reprogramarReserva(editando, new Date(horario.inicio).toISOString(), new Date(horario.fin).toISOString())
        .then(() => { setExito('Reserva reprogramada.'); setEditando(null) })
        .catch((e: unknown) => setError(e instanceof Error ? e.message : 'No se pudo reprogramar.'))
        .finally(() => setProcesando(null)) }}>
        <h3>Reprogramar reserva</h3><p>Disponible hasta una hora antes del inicio. El nuevo horario debe tener al menos 2 horas de anticipación.</p>
        <div className="reserva-horario-grid"><label>Nuevo inicio<input type="datetime-local" required value={horario.inicio} onChange={(e) => setHorario({ ...horario, inicio: e.target.value })} /></label>
        <label>Nuevo fin<input type="datetime-local" required value={horario.fin} onChange={(e) => setHorario({ ...horario, fin: e.target.value })} /></label></div>
        <div className="acciones-fila"><button className="boton primario" type="submit">Guardar horario</button><button className="boton secundario" type="button" onClick={() => setEditando(null)}>Cancelar</button></div>
      </form>}
      <Tabla filas={mias} columnas={columnas} vacio="Todavía no tienes reservas registradas" />
    </section>
  )
}
