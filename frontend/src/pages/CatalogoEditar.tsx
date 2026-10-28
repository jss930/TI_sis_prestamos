import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem } from '../types'

export default function CatalogoEditar({ itemId, catalogo, actualizar }: { itemId?: string; catalogo: CatalogoItem[]; actualizar: (id: string, titulo: string) => Promise<void> }) {
  const navegar = useNavigate(); const item = catalogo.find((i) => i.id === itemId)
  const [titulo, setTitulo] = useState(item?.titulo ?? ''); const [guardando, setGuardando] = useState(false); const [error, setError] = useState<string | null>(null)
  async function enviar(e: FormEvent) { e.preventDefault(); if (!itemId) return; setGuardando(true); setError(null); try { await actualizar(itemId, titulo); navegar('/admin/catalogo', { replace: true }) } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo actualizar.') } finally { setGuardando(false) } }
  return <><PageHeader volverA="/admin/catalogo" titulo="Editar título" descripcion="Modifica el nombre visible del recurso." />{error && <div className="aviso error-aviso">{error}</div>}
    <form className="panel formulario tarea-form" onSubmit={enviar}><label>Título<input autoFocus required value={titulo} onChange={(e) => setTitulo(e.target.value)} /></label><div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/admin/catalogo')}>Cancelar</button><button className="boton primario" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar cambios'}</button></div></form></>
}
