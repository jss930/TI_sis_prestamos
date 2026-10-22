import { useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { CatalogoItem, Reserva, Usuario } from '../types'
import { fechaCorta, nombreUsuario, tituloDeCatalogo } from '../utils/format'

interface Props {
  reservas: Reserva[]
  usuarios: Usuario[]
  catalogo: CatalogoItem[]
  operadorId: string | null
  aceptarReserva: (id: string) => Promise<void>
  denegarReserva: (id: string, motivo: string) => Promise<void>
}

export default function Reservas({
  reservas,
  usuarios,
  catalogo,
  operadorId,
  aceptarReserva,
  denegarReserva,
}: Props) {
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState<string | null>(null)
  const [motivos, setMotivos] = useState<Record<string, string>>({})

  const pendientes = reservas
    .filter((r) => r.estado === 'SOLICITADA')
    .sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))

  async function aceptar(id: string) {
    if (!operadorId) return
    setError(null)
    setProcesando(id)
    setError(null)
    setExito(null)
    try {
      await aceptarReserva(id)
      setExito('Reserva aceptada y préstamo creado.')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo aceptar la reserva.')
    } finally {
      setProcesando(null)
    }
  }

  async function denegar(id: string) {
    if (!operadorId) return
    const motivo = motivos[id]?.trim()
    if (!motivo) {
      setError('El motivo de denegación es obligatorio.')
      return
    }
    if (!window.confirm('¿Denegar esta reserva?')) return
    setError(null)
    setExito(null)
    setProcesando(id)
    try {
      await denegarReserva(id, motivo)
      setExito('Reserva denegada.')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo denegar la reserva.')
    } finally {
      setProcesando(null)
    }
  }

  const columnas: Columna<Reserva>[] = [
    {
      clave: 'prestatario',
      titulo: 'PRESTATARIO',
      render: (r) => <strong>{nombreUsuario(usuarios, r.prestatarioId)}</strong>,
    },
    {
      clave: 'item',
      titulo: 'ÍTEM',
      render: (r) => tituloDeCatalogo(catalogo, r.catalogoItemId),
    },
    {
      clave: 'fecha',
      titulo: 'FECHA',
      render: (r) => fechaCorta(r.fechaAlta.slice(0, 10)),
    },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (r) => <EtiquetaEstado estado={r.estado} />,
    },
    {
      clave: 'acciones',
      titulo: 'ACCIONES',
      render: (r) => (
        <div className="acciones-fila">
          <button
            className="boton primario"
            disabled={procesando === r.id || !operadorId}
            onClick={() => void aceptar(r.id)}
            type="button"
          >
            {procesando === r.id ? 'Procesando…' : 'Aceptar'}
          </button>
          <input
            placeholder="Motivo"
            value={motivos[r.id] ?? ''}
            onChange={(e) => setMotivos((prev) => ({ ...prev, [r.id]: e.target.value }))}
          />
          <button
            className="boton secundario"
            disabled={procesando === r.id || !operadorId}
            onClick={() => void denegar(r.id)}
            type="button"
          >
            Denegar
          </button>
        </div>
      ),
    },
  ]

  return (
    <section className="panel">
      <div className="panel-titulo">
        <div>
          <h2>Reservas pendientes</h2>
          <p>{pendientes.length} solicitudes por revisar</p>
        </div>
      </div>
      {error && <div className="aviso error-aviso">{error}</div>}
      {exito && <div className="aviso">{exito}</div>}
      <Tabla filas={pendientes} columnas={columnas} vacio="No hay reservas pendientes" />
    </section>
  )
}
