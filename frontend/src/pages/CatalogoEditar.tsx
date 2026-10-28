import { useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem } from '../types'

export default function CatalogoEditar({ catalogo, actualizar }: { catalogo: CatalogoItem[]; actualizar: (id: string, titulo: string) => Promise<void> }) {
  const { id } = useParams(); const navegar = useNavigate(); const item = catalogo.find((i) => i.id === id)
  const [titulo, setTitulo] = useState(item?.titulo ?? ''); const [guardando, setGuardando] = useState(false); const [error, setError] = useState<string | null>(null)
  async function enviar(e: FormEvent) { e.preventDefault(); if (!id) return; setGuardando(true); setError(null); try { await actualizar(id, titulo); navegar('/admin/catalogo', { replace: true }) } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo actualizar.') } finally { setGuardando(false) } }
  return <><PageHeader volverA="/admin/catalogo" titulo="Editar título" descripcion="Modifica el nombre visible del recurso." />{error && <div className="aviso error-aviso">{error}</div>}
    <form className="panel formulario tarea-form" onSubmit={enviar}><label>Título<input autoFocus required value={titulo} onChange={(e) => setTitulo(e.target.value)} /></label><div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/admin/catalogo')}>Cancelar</button><button className="boton primario" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar cambios'}</button></div></form></>
}
