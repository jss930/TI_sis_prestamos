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
}

export default function MisReservas({ reservas, catalogo, usuarioId, cancelarReserva }: Props) {
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState<string | null>(null)

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
      titulo: 'FECHA',
      render: (r) => fechaCorta(r.fechaAlta.slice(0, 10)),
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
        (r.estado === 'SOLICITADA' || r.estado === 'APROBADA') ? (
          <button
            className="boton secundario"
            disabled={procesando === r.id}
            onClick={() => void cancelar(r.id)}
            type="button"
          >
            {procesando === r.id ? 'Cancelando…' : 'Cancelar'}
          </button>
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
      <Tabla filas={mias} columnas={columnas} vacio="Todavía no tienes reservas registradas" />
    </section>
  )
}
