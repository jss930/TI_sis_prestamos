import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader'
import type { CatalogoItem, Ejemplar, LugarUso, NuevoPrestamo, Usuario } from '../types'
import { descripcionPlazo, finMaximo, HORA, instanteLima, permiteDomicilio, validarPlazo, valorLima } from '../utils/plazos'

export default function PrestamoNuevo({ usuarios, catalogo, ejemplares, operadorId, crear }: {
  usuarios: Usuario[]; catalogo: CatalogoItem[]; ejemplares: Ejemplar[]; operadorId: string; crear: (v: NuevoPrestamo) => Promise<void>
}) {
  const navegar = useNavigate()
  const [busqueda, setBusqueda] = useState('')
  const [prestatarioId, setPrestatario] = useState('')
  const [itemId, setItem] = useState('')
  const [ejemplarId, setEjemplar] = useState('')
  const [lugar, setLugar] = useState<LugarUso>('EN_CAMPUS')
  const [entrega, setEntrega] = useState(() => valorLima(new Date()))
  const [vencimiento, setVencimiento] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const personas = useMemo(() => {
    const q = busqueda.toLowerCase().trim()
    return usuarios.filter((u) => u.estado === 'VALIDADO' && u.id !== operadorId
      && `${u.nombreCompleto} ${u.email} ${u.codigoInstitucional ?? ''}`.toLowerCase().includes(q)).slice(0, 8)
  }, [usuarios, operadorId, busqueda])
  const persona = usuarios.find((u) => u.id === prestatarioId)
  const item = catalogo.find((i) => i.id === itemId)
  const domicilio = !!persona && !!item && permiteDomicilio(persona.perfil, item.tipo)
  const copias = ejemplares.filter((e) => e.catalogoItemId === itemId && e.estadoFisico === 'DISPONIBLE')
  const maximo = persona && item ? finMaximo(entrega, lugar, persona.perfil, item.tipo) : ''

  useEffect(() => {
    if (!domicilio && lugar === 'DOMICILIO') { setLugar('EN_CAMPUS'); return }
    setVencimiento(maximo)
  }, [domicilio, lugar, maximo, prestatarioId, itemId])

  async function enviar(e: FormEvent) {
    e.preventDefault()
    if (enviando) return
    setError(null)
    try {
      if (!persona || !item || !ejemplarId || !vencimiento) throw new Error('Completa la persona, el bien y el horario.')
      validarPlazo(entrega, vencimiento, lugar, persona.perfil, item.tipo)
      if (instanteLima(entrega).getTime() > Date.now() + 5 * 60000) throw new Error('La entrega presencial no puede registrarse en el futuro.')
      setEnviando(true)
      await crear({ operadorId, prestatarioId, ejemplarId, lugarUso: lugar,
        fechaHoraEntrega: instanteLima(entrega).toISOString(), fechaHoraVencimiento: instanteLima(vencimiento).toISOString() })
      navegar('/prestamos', { replace: true, state: { mensaje: 'Préstamo registrado correctamente.' } })
    } catch (ex) { setError(ex instanceof Error ? ex.message : 'No se pudo registrar el préstamo.') }
    finally { setEnviando(false) }
  }

  return <>
    <PageHeader volverA="/prestamos" titulo="Registrar préstamo" descripcion="Entrega presencial. Todos los horarios se expresan en hora de Lima." />
    {error && <div className="aviso error-aviso" role="alert">{error}</div>}
    <form className="panel formulario tarea-form" onSubmit={enviar}>
      <fieldset disabled={enviando} className="formulario-sin-borde">
        <legend>Datos del préstamo</legend>
        <section className="form-seccion"><span className="seccion-numero">1</span><div><h2>Persona</h2><p>Busca por nombre, correo o código institucional.</p></div></section>
        {persona ? <div className="seleccion-card"><div><strong>{persona.nombreCompleto}</strong><span>{persona.email} · {persona.perfil}</span></div>
          <button className="boton terciario" type="button" onClick={() => { setPrestatario(''); setBusqueda(''); setEjemplar('') }}>Cambiar</button></div>
          : <><label>Buscar persona<input autoFocus type="search" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} placeholder="Nombre, correo o código" /></label>
            {busqueda && <div className="combobox-resultados">{personas.map((u) => <button type="button" key={u.id} onClick={() => setPrestatario(u.id)}>
              <strong>{u.nombreCompleto}</strong><span>{u.email} · {u.perfil}</span></button>)}{!personas.length && <p>No hay cuentas validadas coincidentes.</p>}</div>}</>}
        <section className="form-seccion"><span className="seccion-numero">2</span><div><h2>Bien y copia</h2><p>Solo se muestran copias disponibles.</p></div></section>
        <div className="campos dos">
          <label>Título<select required value={itemId} onChange={(e) => { setItem(e.target.value); setEjemplar('') }}><option value="">Seleccionar…</option>{catalogo.filter((i) => i.activo).map((i) => <option key={i.id} value={i.id}>{i.titulo}</option>)}</select></label>
          <label>Ejemplar<select required disabled={!itemId} value={ejemplarId} onChange={(e) => setEjemplar(e.target.value)}><option value="">Seleccionar copia…</option>{copias.map((c) => <option key={c.id} value={c.id}>{c.barcode} · {c.sala}, {c.estante}</option>)}</select></label>
          {domicilio ? <label>Lugar de uso<select value={lugar} onChange={(e) => setLugar(e.target.value as LugarUso)}><option value="EN_CAMPUS">En campus</option><option value="DOMICILIO">A domicilio</option></select></label>
            : <p>Uso en campus. El domicilio no está disponible para esta selección.</p>}
        </div>
        <section className="form-seccion"><span className="seccion-numero">3</span><div><h2>Horario (Lima)</h2><p>Confirma la hora de entrega y devolución.</p></div></section>
        <div className="campos dos">
          <label>Fecha y hora de entrega<input type="datetime-local" required value={entrega} max={valorLima(new Date(Date.now() + 5 * 60000))} onChange={(e) => setEntrega(e.target.value)} /></label>
          <label>Fecha y hora de devolución<input type="datetime-local" required value={vencimiento}
            min={entrega ? valorLima(new Date(instanteLima(entrega).getTime() + HORA)) : undefined}
            max={maximo || undefined} onChange={(e) => setVencimiento(e.target.value)} /></label>
        </div>
        {persona && item && <p role="status">{descripcionPlazo(lugar, persona.perfil, item.tipo)}</p>}
        <div className="acciones-finales"><button className="boton secundario" type="button" onClick={() => navegar('/prestamos')}>Cancelar</button>
          <button className="boton primario" disabled={!persona || !item || !ejemplarId}>{enviando ? 'Registrando…' : 'Confirmar préstamo'}</button></div>
      </fieldset>
    </form>
  </>
}
