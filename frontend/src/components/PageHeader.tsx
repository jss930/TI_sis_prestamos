import type { ReactNode } from 'react'
import Volver from './Volver'

export default function PageHeader({ titulo, descripcion, volverA, acciones }: {
  titulo: string; descripcion?: string; volverA?: string; acciones?: ReactNode
}) {
  return <header className="page-header">
    <div>{volverA && <Volver a={volverA} />}<h1>{titulo}</h1>{descripcion && <p>{descripcion}</p>}</div>
    {acciones && <div className="page-actions">{acciones}</div>}
  </header>
}
