import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { CatalogoItem, Ejemplar, Prestamo } from '../types'
import { etiquetaLugar, fechaCorta, tituloDeEjemplar } from '../utils/format'

interface Props {
  prestamos: Prestamo[]
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
}

export default function MisPrestamos({ prestamos, catalogo, ejemplares }: Props) {
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
      render: (prestamo) => fechaCorta(prestamo.fechaInicio),
    },
    {
      clave: 'vencimiento',
      titulo: 'VENCE',
      render: (prestamo) => (
        <div className="celda-principal">
          <span>{fechaCorta(prestamo.fechaVencimiento)}</span>
          {prestamo.vencido && (
            <small className="texto-peligro">{prestamo.diasRetraso} días de retraso</small>
          )}
        </div>
      ),
    },
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
        </div>
      </div>
      <Tabla filas={ordenados} columnas={columnas} vacio="Todavía no tienes préstamos registrados" />
    </section>
  )
}