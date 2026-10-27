import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { DatosBiblioteca, Pagina, Prestamo } from '../types'
import { fechaCorta, nombreUsuario, tituloDeEjemplar } from '../utils/format'

interface Props {
  datos: DatosBiblioteca
  irA: (pagina: Pagina) => void
}

export default function Resumen({ datos, irA }: Props) {
  const { prestamos, usuarios, catalogo, ejemplares, sanciones } = datos
  const activos = prestamos.filter((prestamo) => prestamo.estado === 'ACTIVO')
  const vencidos = activos.filter((prestamo) => prestamo.vencido)
  const disponibles = ejemplares.filter((ejemplar) => ejemplar.estadoFisico === 'DISPONIBLE')
  const sancionesActivas = sanciones.filter((sancion) => sancion.estado === 'ACTIVA')
  const itemsActivos = catalogo.filter((item) => item.activo)

  const tarjetas = [
    {
      titulo: 'Préstamos activos',
      valor: activos.length,
      detalle: vencidos.length ? `${vencidos.length} vencidos` : 'Sin vencidos',
      icono: '↗',
    },
    {
      titulo: 'Bienes disponibles',
      valor: disponibles.length,
      detalle: `de ${ejemplares.length} ejemplares`,
      icono: '▦',
    },
    {
      titulo: 'Catálogo activo',
      valor: itemsActivos.length,
      detalle: `de ${catalogo.length} registros`,
      icono: '▤',
    },
    {
      titulo: 'Sanciones activas',
      valor: sancionesActivas.length,
      detalle: sancionesActivas.length ? 'requieren revisión' : 'Sin restricciones',
      icono: '!',
    },
  ]

  const recientes = [...prestamos]
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
          <article className="tarjeta" key={tarjeta.titulo}>
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
          <button className="boton secundario" onClick={() => irA('prestamos')} type="button">
            Ver todos →
          </button>
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
