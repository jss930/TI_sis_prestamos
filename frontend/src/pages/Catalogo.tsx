import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { CatalogoItem, Ejemplar } from '../types'
import { descripcionItem } from '../utils/format'
import { imagenCatalogo } from '../utils/imagenesCatalogo'

const FILTROS: Array<{ id: 'TODOS' | CatalogoItem['tipo']; texto: string }> = [
  { id: 'TODOS', texto: 'Todos' }, { id: 'LIBRO', texto: 'Libros' },
  { id: 'EQUIPO', texto: 'Equipos' }, { id: 'OTRO', texto: 'Otros' },
]

export default function Catalogo({ catalogo, ejemplares, puedeReservar, puedeGestionar,
  itemsReservados = [] }: { catalogo: CatalogoItem[]; ejemplares: Ejemplar[];
  puedeReservar: boolean; puedeGestionar: boolean; itemsReservados?: string[] }) {
  const navegar = useNavigate()
  const [tipo, setTipo] = useState<'TODOS' | CatalogoItem['tipo']>('TODOS')
  const [busqueda, setBusqueda] = useState('')
  const disponibles = useMemo(() => ejemplares.reduce<Record<string, number>>((total, ejemplar) => {
    if (ejemplar.estadoFisico === 'DISPONIBLE') total[ejemplar.catalogoItemId] = (total[ejemplar.catalogoItemId] ?? 0) + 1
    return total
  }, {}), [ejemplares])
  const visibles = useMemo(() => {
    const q = busqueda.trim().toLocaleLowerCase('es')
    return catalogo.filter((item) => (tipo === 'TODOS' || item.tipo === tipo)
      && [item.titulo, item.detalle.isbn, descripcionItem(item)].filter(Boolean).join(' ').toLocaleLowerCase('es').includes(q))
  }, [catalogo, tipo, busqueda])

  return <>
    <section className="catalogo-toolbar" aria-label="Herramientas del catálogo">
      <label className="busqueda-catalogo"><span>Buscar título, autor o ISBN</span>
        <input className="buscador" type="search" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} placeholder="Empieza a escribir…" />
      </label>
      {puedeGestionar && <button className="boton secundario" onClick={() => navegar('/admin/catalogo')} type="button">Gestionar catálogo</button>}
    </section>
    <div className="filtros" aria-label="Filtrar por tipo">{FILTROS.map((filtro) => <button key={filtro.id}
      className={tipo === filtro.id ? 'activo' : ''} onClick={() => setTipo(filtro.id)} type="button">{filtro.texto}</button>)}</div>
    <p className="resultado-conteo" role="status">{visibles.length} {visibles.length === 1 ? 'resultado' : 'resultados'}</p>
    <section className="catalogo-grid">{visibles.map((item) => {
      const cantidad = disponibles[item.id] ?? 0
      const activo = item.activo && cantidad > 0
      const reservada = itemsReservados.includes(item.id)
      return <article className="item" key={item.id}>
        <div className={`item-imagen ${item.tipo.toLowerCase()}`}><img src={imagenCatalogo(item)} alt="" loading="lazy" /></div>
        <div className="item-tipo">{item.tipo}</div><h3>{item.titulo}</h3>
        <p className="item-descripcion">{descripcionItem(item)}</p>
        <div className="disponibilidad"><span className={activo ? 'punto' : 'punto no'} />{activo ? `${cantidad} disponibles` : 'No disponible'}</div>
        <div className="item-accion">{puedeReservar && <button className="boton primario" type="button"
          disabled={!activo || reservada} onClick={() => navegar(`/catalogo/${item.id}/reservar`)}>
          {reservada ? 'Reserva activa' : 'Reservar'}</button>}</div>
      </article>
    })}</section>
    {!visibles.length && <div className="empty-state"><strong>No encontramos resultados</strong><p>Prueba otra palabra o cambia el filtro.</p></div>}
  </>
}
