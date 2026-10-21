import { useState } from 'react'
import type { CatalogoItem, Ejemplar, Pagina, TipoItem } from '../types'
import { descripcionItem } from '../utils/format'

const CATEGORIAS: Array<{ id: 'TODOS' | TipoItem; texto: string }> = [
  { id: 'TODOS', texto: 'Todos' },
  { id: 'LIBRO', texto: 'Libros' },
  { id: 'EQUIPO', texto: 'Equipos' },
  { id: 'OTRO', texto: 'Otros' },
]

const ICONOS: Record<TipoItem, string> = {
  LIBRO: '▤',
  EQUIPO: '▣',
  OTRO: '●',
}

interface Props {
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
  puedeSolicitar: boolean
  irA: (pagina: Pagina) => void
}

export default function Catalogo({ catalogo, ejemplares, puedeSolicitar, irA }: Props) {
  const [categoria, setCategoria] = useState<'TODOS' | TipoItem>('TODOS')

  const disponiblesPorItem = new Map<string, number>()
  for (const ejemplar of ejemplares) {
    if (ejemplar.estadoFisico === 'DISPONIBLE') {
      disponiblesPorItem.set(
        ejemplar.catalogoItemId,
        (disponiblesPorItem.get(ejemplar.catalogoItemId) ?? 0) + 1,
      )
    }
  }

  const visibles =
    categoria === 'TODOS' ? catalogo : catalogo.filter((item) => item.tipo === categoria)

  return (
    <>
      <div className="filtros">
        {CATEGORIAS.map((opcion) => (
          <button
            className={categoria === opcion.id ? 'activo' : ''}
            onClick={() => setCategoria(opcion.id)}
            key={opcion.id}
            type="button"
          >
            {opcion.texto}
          </button>
        ))}
      </div>
      <section className="catalogo-grid">
        {visibles.map((item) => {
          const disponibles = disponiblesPorItem.get(item.id) ?? 0
          const habilitado = item.activo && disponibles > 0
          return (
            <article className="item" key={item.id}>
              <div className={`item-imagen ${item.tipo.toLowerCase()}`}>{ICONOS[item.tipo]}</div>
              <small>
                {item.tipo} · {item.activo ? 'Activo' : 'Inactivo'}
              </small>
              <h3>{item.titulo}</h3>
              <p className="item-descripcion">{descripcionItem(item)}</p>
              {item.tipo === 'LIBRO' && item.detalle.isbn && (
                <p className="item-meta">ISBN {item.detalle.isbn}</p>
              )}
              <div className="disponibilidad">
                <span className={habilitado ? 'punto' : 'punto no'} />
                {habilitado ? `${disponibles} disponibles` : 'No disponible'}
              </div>
              <button
                className="boton secundario"
                disabled={!habilitado || !puedeSolicitar}
                onClick={() => irA('prestamos')}
                type="button"
              >
                {puedeSolicitar ? 'Solicitar préstamo' : 'Consulta en ventanilla'}
              </button>
            </article>
          )
        })}
      </section>
      {!visibles.length && <div className="aviso">No hay bienes en esta categoría.</div>}
    </>
  )
}
