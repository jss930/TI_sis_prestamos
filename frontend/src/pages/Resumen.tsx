import { useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { DatosBiblioteca, Pagina, PermisoOperativo, Prestamo } from '../types'
import { fechaCorta, nombreUsuario, tituloDeEjemplar } from '../utils/format'

interface Props {
  datos: DatosBiblioteca
  irA: (pagina: Pagina) => void
  permisos: PermisoOperativo[]
}

type FiltroResumen = 'TODOS' | 'ACTIVOS' | 'VENCIDOS'

export default function Resumen({ datos, irA, permisos }: Props) {
  const [filtro, setFiltro] = useState<FiltroResumen>('TODOS')
  const { prestamos, usuarios, catalogo, ejemplares, sanciones } = datos
  const activos = prestamos.filter((prestamo) => prestamo.estado === 'ACTIVO')
  const vencidos = activos.filter((prestamo) => prestamo.vencido)
  const disponibles = ejemplares.filter((ejemplar) => ejemplar.estadoFisico === 'DISPONIBLE')
  const sancionesActivas = sanciones.filter((sancion) => sancion.estado === 'ACTIVA')
  const itemsActivos = catalogo.filter((item) => item.activo)

  const puedeVerPrestamos = permisos.includes('REGISTRAR_PRESTAMO') || permisos.includes('REGISTRAR_DEVOLUCION')
  const tarjetas: Array<{ titulo: string; valor: number; detalle: string; icono: string; destino?: Pagina }> = [
    {
      titulo: 'Préstamos activos',
      valor: activos.length,
      detalle: vencidos.length ? `${vencidos.length} vencidos` : 'Sin vencidos',
      icono: '↗',
      destino: puedeVerPrestamos ? 'prestamos' : undefined,
    },
    {
      titulo: 'Bienes disponibles',
      valor: disponibles.length,
      detalle: `de ${ejemplares.length} ejemplares`,
      icono: '▦',
      destino: 'catalogo',
    },
    {
      titulo: 'Catálogo activo',
      valor: itemsActivos.length,
      detalle: `de ${catalogo.length} registros`,
      icono: '▤',
      destino: 'catalogo',
    },
    {
      titulo: 'Sanciones activas',
      valor: sancionesActivas.length,
      detalle: sancionesActivas.length ? 'requieren revisión' : 'Sin restricciones',
      icono: '!',
      destino: permisos.includes('GESTIONAR_SANCIONES') ? 'sanciones' : undefined,
    },
  ]

  const recientes = prestamos
    .filter((prestamo) => filtro === 'TODOS'
      || (filtro === 'ACTIVOS' && prestamo.estado === 'ACTIVO' && !prestamo.vencido)
      || (filtro === 'VENCIDOS' && prestamo.estado === 'ACTIVO' && prestamo.vencido))
    .sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio))
    .slice(0, 5)

  const columnas: Columna<Prestamo>[] = [
    {
      clave: 'persona',
      titulo: 'PERSONA',
      render: (prestamo) => nombreUsuario(usuarios, prestamo.prestatarioId),
    },
    {
      clave: 'bien',
      titulo: 'BIEN',
      render: (prestamo) => tituloDeEjemplar(ejemplares, catalogo, prestamo.ejemplarId),
    },
    {
      clave: 'devolucion',
      titulo: 'DEVOLUCIÓN',
      render: (prestamo) => fechaCorta(prestamo.fechaVencimiento),
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
    <>
      <section className="tarjetas">
        {tarjetas.map((tarjeta) => (
          <article
            className={`tarjeta${tarjeta.destino ? ' navegable' : ''}`}
            key={tarjeta.titulo}
            onClick={tarjeta.destino ? () => irA(tarjeta.destino!) : undefined}
            onKeyDown={tarjeta.destino ? (evento) => {
              if (evento.key === 'Enter' || evento.key === ' ') {
                evento.preventDefault()
                irA(tarjeta.destino!)
              }
            } : undefined}
            role={tarjeta.destino ? 'link' : undefined}
            tabIndex={tarjeta.destino ? 0 : undefined}
            aria-label={tarjeta.destino ? `Ver ${tarjeta.titulo.toLowerCase()}` : undefined}
          >
            <div>
              <span>{tarjeta.titulo}</span>
              <strong>{tarjeta.valor}</strong>
              <small>{tarjeta.detalle}</small>
            </div>
            <b>{tarjeta.icono}</b>
          </article>
        ))}
      </section>
      <section className="panel">
        <div className="panel-titulo">
          <div>
            <h2>Préstamos recientes</h2>
            <p>Consulta las últimas entregas y sus fechas de devolución</p>
          </div>
          <div className="resumen-controles">
            <div className="filtros resumen-filtros" aria-label="Filtrar préstamos recientes">
              {(['TODOS', 'ACTIVOS', 'VENCIDOS'] as FiltroResumen[]).map((opcion) => (
                <button className={filtro === opcion ? 'activo' : ''} key={opcion} onClick={() => setFiltro(opcion)} type="button" aria-pressed={filtro === opcion}>
                  {opcion === 'TODOS' ? 'Todos' : opcion === 'ACTIVOS' ? 'Activos' : 'Vencidos'}
                </button>
              ))}
            </div>
            {puedeVerPrestamos && <button className="boton secundario" onClick={() => irA('prestamos')} type="button">Ver todos →</button>}
          </div>
        </div>
        <Tabla
          filas={recientes}
          columnas={columnas}
          vacio="Todavía no hay préstamos registrados"
        />
      </section>
    </>
  )
}
