import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, NuevoEjemplar } from '../types'

export default function EjemplarAlta({ itemId, catalogo, crear }: { itemId?: string; catalogo: CatalogoItem[]; crear: (v: NuevoEjemplar) => Promise<void> }) {
  const navegar = useNavigate(); const item = catalogo.find((i) => i.id === itemId)
  const [datos, setDatos] = useState({ barcode: '', sala: '', estante: '', notas: '' }); const [error, setError] = useState<string | null>(null); const [guardando, setGuardando] = useState(false)
  async function enviar(e: FormEvent) { e.preventDefault(); if (!itemId) return; setGuardando(true); setError(null); try { await crear({ catalogoItemId: itemId, ...datos, notas: datos.notas || null }); navegar('/admin/catalogo', { replace: true }) } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo guardar.') } finally { setGuardando(false) } }
  return <><PageHeader volverA="/admin/catalogo" titulo="Añadir ejemplar" descripcion={item ? `Nueva copia física de “${item.titulo}”.` : 'Registra una nueva copia física.'} />
    {error && <div className="aviso error-aviso">{error}</div>}<form className="panel formulario tarea-form" onSubmit={enviar}><div className="campos dos">
      <label>Código de inventario<input required autoFocus placeholder="Ej. LIB-004" value={datos.barcode} onChange={(e) => setDatos({ ...datos, barcode: e.target.value })} /><small>Debe ser único.</small></label>
      <label>Sala o ambiente<input required placeholder="Biblioteca central" value={datos.sala} onChange={(e) => setDatos({ ...datos, sala: e.target.value })} /></label>
      <label>Ubicación exacta<input required placeholder="Estante B, nivel 2" value={datos.estante} onChange={(e) => setDatos({ ...datos, estante: e.target.value })} /></label>
      <label>Notas opcionales<textarea rows={3} value={datos.notas} onChange={(e) => setDatos({ ...datos, notas: e.target.value })} /></label></div>
      <div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/admin/catalogo')}>Cancelar</button><button className="boton primario" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar ejemplar'}</button></div>
    </form></>
}
