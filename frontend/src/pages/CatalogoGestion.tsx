import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, Ejemplar } from '../types'
import { descripcionItem } from '../utils/format'

export default function CatalogoGestion({ catalogo, ejemplares, activar, desactivar }: { catalogo: CatalogoItem[]; ejemplares: Ejemplar[]; activar: (id: string) => Promise<void>; desactivar: (id: string) => Promise<void> }) {
  const navegar = useNavigate(); const [q, setQ] = useState(''); const [procesando, setProcesando] = useState<string | null>(null)
  const items = useMemo(() => catalogo.filter((i) => `${i.titulo} ${descripcionItem(i)}`.toLowerCase().includes(q.toLowerCase())), [catalogo, q])
  async function estado(item: CatalogoItem) { setProcesando(item.id); try { await (item.activo ? desactivar(item.id) : activar(item.id)) } finally { setProcesando(null) } }
  return <><PageHeader volverA="/catalogo" titulo="Gestión del catálogo" descripcion="Administra títulos y copias sin mezclar estas tareas con la consulta pública."
    acciones={<button className="boton primario" onClick={() => navegar('/admin/catalogo/nuevo')}>+ Nuevo título</button>} />
    <section className="panel"><label className="busqueda-catalogo">Buscar en el catálogo<input className="buscador" type="search" value={q} onChange={(e) => setQ(e.target.value)} /></label>
      <div className="tabla-contenedor"><table><thead><tr><th>TÍTULO</th><th>TIPO</th><th>COPIAS</th><th>ESTADO</th><th>ACCIONES</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td><strong>{item.titulo}</strong><small className="tabla-subtexto">{descripcionItem(item)}</small></td><td>{item.tipo}</td><td>{ejemplares.filter((e) => e.catalogoItemId === item.id).length}</td><td><span className={`status-dot ${item.activo ? 'ok' : ''}`}>{item.activo ? 'Activo' : 'Inactivo'}</span></td><td><div className="acciones-fila"><button className="boton terciario" onClick={() => navegar(`/admin/catalogo/${item.id}/editar`)}>Editar</button><button className="boton terciario" onClick={() => navegar(`/admin/catalogo/${item.id}/ejemplares/nuevo`)}>Añadir ejemplar</button><button className="boton terciario" disabled={procesando === item.id} onClick={() => void estado(item)}>{item.activo ? 'Desactivar' : 'Activar'}</button></div></td></tr>)}</tbody></table></div>
    </section></>
}
