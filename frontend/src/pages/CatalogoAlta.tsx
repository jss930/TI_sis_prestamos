import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { NuevoEquipoCatalogo, NuevoLibroCatalogo, NuevoOtroCatalogo } from '../types'

export default function CatalogoAlta({ crearLibro, crearEquipo, crearOtro }: {
  crearLibro: (v: NuevoLibroCatalogo) => Promise<void>; crearEquipo: (v: NuevoEquipoCatalogo) => Promise<void>; crearOtro: (v: NuevoOtroCatalogo) => Promise<void>
}) {
  const navegar = useNavigate(); const [tipo, setTipo] = useState<'LIBRO' | 'EQUIPO' | 'OTRO'>('LIBRO'); const [error, setError] = useState<string | null>(null); const [guardando, setGuardando] = useState(false)
  const [libro, setLibro] = useState<NuevoLibroCatalogo>({ titulo: '', isbn: '', autor: '', editorial: '', anioPublicacion: new Date().getFullYear() })
  const [equipo, setEquipo] = useState<NuevoEquipoCatalogo>({ titulo: '', marca: '', modelo: '' }); const [otro, setOtro] = useState<NuevoOtroCatalogo>({ titulo: '', descripcion: '' })
  async function enviar(e: FormEvent) { e.preventDefault(); setGuardando(true); setError(null); try { if (tipo === 'LIBRO') await crearLibro(libro); else if (tipo === 'EQUIPO') await crearEquipo(equipo); else await crearOtro(otro); navegar('/admin/catalogo', { replace: true }) } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo guardar.') } finally { setGuardando(false) } }
  return <><PageHeader volverA="/admin/catalogo" titulo="Nuevo título" descripcion="Primero registra la información bibliográfica. Después podrás añadir sus copias físicas." />
    {error && <div className="aviso error-aviso">{error}</div>}<form className="panel formulario tarea-form" onSubmit={enviar}>
      <label>Tipo de recurso<select value={tipo} onChange={(e) => setTipo(e.target.value as typeof tipo)}><option value="LIBRO">Libro</option><option value="EQUIPO">Equipo</option><option value="OTRO">Otro recurso</option></select></label>
      {tipo === 'LIBRO' && <div className="campos dos"><label>Título<input required value={libro.titulo} onChange={(e) => setLibro({ ...libro, titulo: e.target.value })} /></label><label>ISBN<input required value={libro.isbn} onChange={(e) => setLibro({ ...libro, isbn: e.target.value })} /></label><label>Autor<input required value={libro.autor} onChange={(e) => setLibro({ ...libro, autor: e.target.value })} /></label><label>Editorial<input required value={libro.editorial} onChange={(e) => setLibro({ ...libro, editorial: e.target.value })} /></label><label>Año<input type="number" required value={libro.anioPublicacion} onChange={(e) => setLibro({ ...libro, anioPublicacion: Number(e.target.value) })} /></label></div>}
      {tipo === 'EQUIPO' && <div className="campos dos"><label>Nombre del equipo<input required value={equipo.titulo} onChange={(e) => setEquipo({ ...equipo, titulo: e.target.value })} /></label><label>Marca<input required value={equipo.marca} onChange={(e) => setEquipo({ ...equipo, marca: e.target.value })} /></label><label>Modelo<input required value={equipo.modelo} onChange={(e) => setEquipo({ ...equipo, modelo: e.target.value })} /></label></div>}
      {tipo === 'OTRO' && <div className="campos dos"><label>Nombre del recurso<input required value={otro.titulo} onChange={(e) => setOtro({ ...otro, titulo: e.target.value })} /></label><label>Descripción<input required value={otro.descripcion} onChange={(e) => setOtro({ ...otro, descripcion: e.target.value })} /></label></div>}
      <div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/admin/catalogo')}>Cancelar</button><button className="boton primario" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar título'}</button></div>
    </form></>
}
