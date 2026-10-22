import { useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import { mensajeDeError } from '../services/api'
import type { CatalogoItem, Ejemplar, EstadoReserva, Prestamo, Reserva, Usuario } from '../types'
import { fechaCorta, nombreUsuario, tituloDeCatalogo } from '../utils/format'

interface Props {
  reservas: Reserva[]
  usuarios: Usuario[]
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
  prestamos: Prestamo[]
  operadorId: string | null
  aceptarReserva: (id: string, ejemplarId: string) => Promise<void>
  denegarReserva: (id: string, motivo: string) => Promise<void>
}

const FILTROS: Array<{ id: EstadoReserva | 'TODAS'; titulo: string }> = [
  { id: 'SOLICITADA', titulo: 'Pendientes' },
  { id: 'ACEPTADA', titulo: 'Aceptadas' },
  { id: 'DENEGADA', titulo: 'Denegadas' },
  { id: 'CANCELADA', titulo: 'Canceladas' },
  { id: 'TODAS', titulo: 'Todas' },
]

export default function Reservas({ reservas, usuarios, catalogo, ejemplares, prestamos,
  operadorId, aceptarReserva, denegarReserva }: Props) {
  const [filtro, setFiltro] = useState<EstadoReserva | 'TODAS'>('SOLICITADA')
  const [busqueda, setBusqueda] = useState('')
  const [seleccionadaId, setSeleccionadaId] = useState<string | null>(null)
  const [ejemplarElegido, setEjemplarElegido] = useState('')
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)
  const seleccionada = reservas.find((reserva) => reserva.id === seleccionadaId)
  const consulta = busqueda.trim().toLocaleLowerCase('es')
  const visibles = reservas.filter((reserva) =>
    (filtro === 'TODAS' || reserva.estado === filtro) &&
    `${nombreUsuario(usuarios, reserva.prestatarioId)} ${tituloDeCatalogo(catalogo, reserva.catalogoItemId)}`
      .toLocaleLowerCase('es').includes(consulta),
  ).sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))
  const disponibles = seleccionada ? ejemplares.filter((ejemplar) =>
    ejemplar.catalogoItemId === seleccionada.catalogoItemId && ejemplar.estadoFisico === 'DISPONIBLE',
  ) : []
  const prestamo = prestamos.find((item) => item.id === seleccionada?.prestamoId)
  const ejemplarEntregado = ejemplares.find((item) => item.id === prestamo?.ejemplarId)

  async function decidir(accion: 'aceptar' | 'denegar') {
    if (!seleccionada || !operadorId || procesando) return
    setError(null)
    setExito(null)
    if (accion === 'denegar' && !motivo.trim()) {
      setError('El motivo de denegación es obligatorio.')
      return
    }
    if (accion === 'aceptar' && !disponibles.some((e) => e.id === ejemplarElegido)) {
      setError('Selecciona un ejemplar disponible para la entrega.')
      return
    }
    setProcesando(true)
    try {
      if (accion === 'aceptar') {
        await aceptarReserva(seleccionada.id, ejemplarElegido)
        setExito('Solicitud aceptada y préstamo registrado. Consulta el ejemplar asignado en el detalle.')
      } else {
        await denegarReserva(seleccionada.id, motivo.trim())
        setExito('Solicitud denegada.')
      }
      setMotivo('')
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setProcesando(false)
    }
  }

  const columnas: Columna<Reserva>[] = [
    { clave: 'persona', titulo: 'PERSONA', render: (r) => nombreUsuario(usuarios, r.prestatarioId) },
    { clave: 'bien', titulo: 'BIEN', render: (r) => tituloDeCatalogo(catalogo, r.catalogoItemId) },
    { clave: 'fecha', titulo: 'SOLICITADO', render: (r) => fechaCorta(r.fechaAlta) },
    { clave: 'estado', titulo: 'ESTADO', render: (r) => <EtiquetaEstado estado={r.estado} /> },
    { clave: 'accion', titulo: 'ACCIÓN', render: (r) => (
      <button className="boton secundario" type="button" disabled={procesando} onClick={() => {
        setSeleccionadaId(r.id)
        setEjemplarElegido('')
        setMotivo('')
        setError(null)
        setExito(null)
      }}>Revisar</button>
    ) },
  ]

  return <>
    <section className="panel">
      <div className="panel-titulo">
        <div><h2>Bandeja de solicitudes y reservas</h2><p>{visibles.length} solicitudes en esta vista</p></div>
        <input className="buscador" type="search" aria-label="Buscar solicitudes por persona o bien"
          placeholder="Buscar por persona o bien" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
      </div>
      <div className="filtros solicitudes-filtros" aria-label="Estado de la solicitud">
        {FILTROS.map((opcion) => <button type="button" key={opcion.id}
          className={filtro === opcion.id ? 'activo' : ''} aria-pressed={filtro === opcion.id}
          onClick={() => setFiltro(opcion.id)}>{opcion.titulo}</button>)}
      </div>
      <Tabla filas={visibles} columnas={columnas} vacio="No hay solicitudes en esta vista" />
    </section>
    {error && <div className="aviso error-aviso" role="alert">{error}</div>}
    {exito && <div className="aviso correcto-aviso" role="status">{exito}</div>}
    {seleccionada && <section className="panel formulario" aria-label="Detalle de la solicitud">
      <h2>Revisión de solicitud</h2>
      <dl className="detalle-solicitud">
        <div><dt>Bien</dt><dd>{tituloDeCatalogo(catalogo, seleccionada.catalogoItemId)}</dd></div>
        <div><dt>Persona</dt><dd>{nombreUsuario(usuarios, seleccionada.prestatarioId)}</dd></div>
        <div><dt>Estado</dt><dd><EtiquetaEstado estado={seleccionada.estado} /></dd></div>
        <div><dt>Fecha</dt><dd>{fechaCorta(seleccionada.fechaAlta)}</dd></div>
        {seleccionada.fechaDecision && <div><dt>Decisión</dt><dd>{fechaCorta(seleccionada.fechaDecision)}</dd></div>}
        {seleccionada.operadorId && <div><dt>Atendida por</dt><dd>{nombreUsuario(usuarios, seleccionada.operadorId)}</dd></div>}
        {seleccionada.motivo && <div><dt>Motivo</dt><dd>{seleccionada.motivo}</dd></div>}
        {seleccionada.prestamoId && <div><dt>Ejemplar asignado</dt><dd>{ejemplarEntregado?.barcode ?? 'Consulta el préstamo asociado'}</dd></div>}
      </dl>
      {seleccionada.estado === 'SOLICITADA' && <>
        <p>{disponibles.length} ejemplares disponibles: {disponibles.map((e) => e.barcode).join(', ') || 'ninguno'}.</p>
        <label>Código de inventario para entregar
          <select value={ejemplarElegido} disabled={procesando || !operadorId} onChange={(e) => setEjemplarElegido(e.target.value)}>
            <option value="">Seleccionar ejemplar…</option>
            {disponibles.map((e) => <option key={e.id} value={e.id}>{e.barcode} — {e.sala}, {e.estante}</option>)}
          </select>
        </label>
        <div className="aviso">Al aceptar se validan las políticas y se registra el préstamo del ejemplar seleccionado.</div>
        <label>Motivo de denegación
          <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} disabled={procesando} rows={3} />
        </label>
        <div className="acciones">
          <button className="boton primario" type="button" disabled={procesando || !operadorId || !disponibles.some((e) => e.id === ejemplarElegido)}
            onClick={() => void decidir('aceptar')}>{procesando ? 'Procesando…' : 'Aceptar y registrar préstamo'}</button>
          <button className="boton secundario" type="button" disabled={procesando || !operadorId}
            onClick={() => void decidir('denegar')}>Denegar solicitud</button>
        </div>
      </>}
      <button className="boton secundario" type="button" disabled={procesando}
        onClick={() => setSeleccionadaId(null)}>Cerrar detalle</button>
    </section>}
  </>
}
