import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { Sancion, Usuario } from '../types'
import { dinero, etiquetaMotivo, fechaCorta } from '../utils/format'

interface Props {
  sanciones: Sancion[]
  usuarios: Usuario[]
}

export default function Sanciones({ sanciones, usuarios }: Props) {
  const activas = sanciones.filter((sancion) => sancion.estado === 'ACTIVA')

  const columnas: Columna<Sancion>[] = [
    {
      clave: 'persona',
      titulo: 'PERSONA',
      render: (sancion) => {
        const usuario = usuarios.find((actual) => actual.id === sancion.usuarioId)
        return (
          <div className="celda-principal">
            <strong>{usuario?.nombreCompleto ?? sancion.usuarioId}</strong>
            <small>{usuario?.email ?? 'Usuario no cargado'}</small>
          </div>
        )
      },
    },
    {
      clave: 'motivo',
      titulo: 'MOTIVO',
      render: (sancion) => etiquetaMotivo(sancion.motivo),
    },
    {
      clave: 'diasSuspension',
      titulo: 'SUSPENSIÓN',
      render: (sancion) => `${sancion.diasSuspension} día(s)`,
    },
    {
      clave: 'multa',
      titulo: 'MULTA',
      render: (sancion) => dinero(sancion.multa, sancion.moneda),
    },
    {
      clave: 'periodo',
      titulo: 'PERIODO',
      render: (sancion) => `${fechaCorta(sancion.fechaInicio)} → ${fechaCorta(sancion.fechaFin)}`,
    },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (sancion) => <EtiquetaEstado estado={sancion.estado} />,
    },
    {
      clave: 'observaciones',
      titulo: 'OBSERVACIONES',
      render: (sancion) => (
        <span className="texto-suave">{sancion.observaciones ?? '—'}</span>
      ),
    },
  ]

  return (
    <>
      <div className="aviso">
        ℹ Las sanciones son calculadas por las reglas del backend. Aquí puedes consultar su
        estado y vigencia.
      </div>
      <section className="panel">
        <div className="panel-titulo">
          <div>
            <h2>Sanciones registradas</h2>
            <p>
              {sanciones.length} en total · {activas.length} activa(s)
            </p>
          </div>
        </div>
        <Tabla
          filas={sanciones}
          columnas={columnas}
          vacio="No hay sanciones registradas"
        />
      </section>
    </>
  )
}
