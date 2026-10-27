import { useEffect, useRef, useState } from 'react'
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
  entregarSolicitud: (id: string) => Promise<void>
  aceptarReserva: (id: string) => Promise<void>
  denegarReserva: (id: string, motivo: string) => Promise<void>
}

const FILTROS: Array<{ id: EstadoReserva | 'TODAS'; titulo: string }> = [
  { id: 'SOLICITADA', titulo: 'Pendientes' },
  { id: 'APROBADA', titulo: 'Por entregar' },
  { id: 'ATENDIDA', titulo: 'Entregadas' },
  { id: 'ACEPTADA', titulo: 'Histórico' },
  { id: 'EXPIRADA', titulo: 'Vencidas sin retiro' },
  { id: 'DENEGADA', titulo: 'Denegadas' },
  { id: 'CANCELADA', titulo: 'Canceladas' },
  { id: 'TODAS', titulo: 'Todas' },
]

export default function Reservas({ reservas, usuarios, catalogo, ejemplares, prestamos,
  operadorId, aceptarReserva, denegarReserva, entregarSolicitud }: Props) {
  const [filtro, setFiltro] = useState<EstadoReserva | 'TODAS'>('SOLICITADA')
  const [busqueda, setBusqueda] = useState('')
  const detalleRef = useRef<HTMLHeadingElement>(null)
  const [revisionDetalle, setRevisionDetalle] = useState(0)
  const [seleccionadaId, setSeleccionadaId] = useState<string | null>(null)
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)
  const seleccionada = reservas.find((reserva) => reserva.id === seleccionadaId)
  useEffect(() => {
    if (!seleccionadaId || !detalleRef.current) return
    detalleRef.current.focus({ preventScroll: true })
    detalleRef.current.scrollIntoView({
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth',
      block: 'start',
    })
  }, [seleccionadaId, revisionDetalle])

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
  const ejemplarEntregado = ejemplares.find((item) => item.id === (seleccionada?.ejemplarId ?? prestamo?.ejemplarId))

  async function decidir(accion: 'aceptar' | 'denegar' | 'entregar') {
    if (!seleccionada || !operadorId || procesando) return
    setError(null)
    setExito(null)
    if (accion === 'denegar' && !motivo.trim()) {
      setError('El motivo de denegación es obligatorio.')
      return
    }
    setProcesando(true)
    try {
      if (accion === 'aceptar') {
        await aceptarReserva(seleccionada.id)
        setExito('Reserva aprobada. El sistema asignó una copia libre para ese horario.')
      } else if (accion === 'entregar') {
        await entregarSolicitud(seleccionada.id)
        setExito('Entrega confirmada. El préstamo comienza hoy.')
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
    { clave: 'horario', titulo: 'HORARIO', render: (r) => r.inicioReserva && r.finReserva
      ? `${new Date(r.inicioReserva).toLocaleString('es-PE')} – ${new Date(r.finReserva).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })}` : 'Histórico' },
    { clave: 'fecha', titulo: 'SOLICITADO', render: (r) => fechaCorta(r.fechaAlta) },
    { clave: 'estado', titulo: 'ESTADO', render: (r) => <EtiquetaEstado estado={r.estado} /> },
    { clave: 'accion', titulo: 'ACCIÓN', render: (r) => (
      <button className="boton secundario" type="button" disabled={procesando} onClick={() => {
        setSeleccionadaId(r.id)
        setRevisionDetalle((revision) => revision + 1)
        setMotivo('')
        setError(null)
        setExito(null)
      }}>Revisar</button>
    ) },
  ]

  return <>
    <section className="panel">
      <div className="panel-titulo">
        <div><h2>Reservas pendientes</h2><p>{visibles.length} reservas en esta vista</p></div>
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
      <h2 ref={detalleRef} tabIndex={-1} style={{ scrollMarginTop: '1.5rem' }}>Revisión de solicitud</h2>
      <dl className="detalle-solicitud">
        <div><dt>Bien</dt><dd>{tituloDeCatalogo(catalogo, seleccionada.catalogoItemId)}</dd></div>
        <div><dt>Persona</dt><dd>{nombreUsuario(usuarios, seleccionada.prestatarioId)}</dd></div>
        <div><dt>Estado</dt><dd><EtiquetaEstado estado={seleccionada.estado} /></dd></div>
        <div><dt>Fecha</dt><dd>{fechaCorta(seleccionada.fechaAlta)}</dd></div>
        {seleccionada.fechaDecision && <div><dt>Decisión</dt><dd>{fechaCorta(seleccionada.fechaDecision)}</dd></div>}
        {seleccionada.operadorId && <div><dt>Atendida por</dt><dd>{nombreUsuario(usuarios, seleccionada.operadorId)}</dd></div>}
        {seleccionada.motivo && <div><dt>Motivo</dt><dd>{seleccionada.motivo}</dd></div>}
        {(seleccionada.ejemplarId || seleccionada.prestamoId) && <div><dt>Ejemplar asignado</dt><dd>{ejemplarEntregado?.barcode ?? 'Consulta el préstamo asociado'}</dd></div>}
        <div><dt>Lugar de uso</dt><dd>{seleccionada.lugarUso === 'DOMICILIO' ? 'A domicilio' : 'Dentro del campus'}</dd></div>
        {seleccionada.inicioReserva && <div><dt>Inicio reservado</dt><dd>{new Date(seleccionada.inicioReserva).toLocaleString('es-PE')}</dd></div>}
        {seleccionada.finReserva && <div><dt>Fin reservado</dt><dd>{new Date(seleccionada.finReserva).toLocaleString('es-PE')}</dd></div>}
        {seleccionada.fechaLimiteRetiro && <div><dt>Plazo de recogida</dt><dd>{new Date(seleccionada.fechaLimiteRetiro).toLocaleString('es-PE')}</dd></div>}
        {seleccionada.entregadoPorId && <div><dt>Entregado por</dt><dd>{nombreUsuario(usuarios, seleccionada.entregadoPorId)}</dd></div>}
        {seleccionada.fechaEntrega && <div><dt>Fecha de entrega</dt><dd>{new Date(seleccionada.fechaEntrega).toLocaleString('es-PE')}</dd></div>}
      </dl>
      {seleccionada.estado === 'APROBADA' && <>
        <p>Confirma cuando el alumno reciba físicamente el artículo. Se volverán a verificar sus condiciones de préstamo.</p>
        <button className="boton primario" type="button"
          disabled={procesando || !operadorId || !seleccionada.fechaLimiteRetiro || Date.now() >= Date.parse(seleccionada.fechaLimiteRetiro)}
          onClick={() => void decidir('entregar')}>{procesando ? 'Procesando…' : 'Confirmar entrega e iniciar préstamo'}</button>
      </>}
      {seleccionada.estado === 'SOLICITADA' && <>
        <p>{disponibles.length} ejemplares disponibles actualmente. El sistema elegirá una copia libre que no tenga otra reserva en ese horario.</p>
        <div className="aviso">La aprobación es manual. El préstamo comienza cuando la persona recoge el bien dentro de su horario.</div>
        <label>Motivo de denegación
          <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} disabled={procesando} rows={3} />
        </label>
        <div className="acciones">
          <button className="boton primario" type="button" disabled={procesando || !operadorId || disponibles.length === 0}
            onClick={() => void decidir('aceptar')}>{procesando ? 'Procesando…' : 'Aprobar reserva'}</button>
          <button className="boton secundario" type="button" disabled={procesando || !operadorId}
            onClick={() => void decidir('denegar')}>Denegar solicitud</button>
        </div>
      </>}
      <button className="boton secundario" type="button" disabled={procesando}
        onClick={() => setSeleccionadaId(null)}>Cerrar detalle</button>
    </section>}
  </>
}
