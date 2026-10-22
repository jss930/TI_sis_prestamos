import { useEffect, useRef, useState, type FormEvent } from 'react'
import type { NuevaInspeccionDevolucion } from '../types'

interface Props {
  bien: string
  persona: string
  enviando: boolean
  confirmar: (inspeccion: NuevaInspeccionDevolucion) => Promise<void>
  cancelar: () => void
}
export default function FormularioInspeccion({ bien, persona, enviando, confirmar, cancelar }: Props) {
  const [inspeccion, setInspeccion] = useState<NuevaInspeccionDevolucion>({
    estadoFisico: 'DISPONIBLE', accesoriosFaltantes: false, observaciones: '',
  })
  const [error, setError] = useState<string | null>(null)
  const selector = useRef<HTMLSelectElement>(null)
  useEffect(() => { selector.current?.focus() }, [])
  const requiereObservaciones = inspeccion.estadoFisico !== 'DISPONIBLE' || inspeccion.accesoriosFaltantes
  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando) return
    if (requiereObservaciones && !inspeccion.observaciones.trim()) {
      setError('Describe los daños, la pérdida o los accesorios faltantes.')
      return
    }
    setError(null)
    await confirmar({ ...inspeccion, observaciones: inspeccion.observaciones.trim() })
  }
  return <section className="panel formulario formulario-inspeccion">
    <h2>Inspección de devolución</h2>
    <p><strong>Bien y código:</strong> {bien}</p><p><strong>Prestado a:</strong> {persona}</p>
    <form onSubmit={(e) => void enviar(e)}>
      <fieldset disabled={enviando}>
        <legend>Recepción del bien</legend>
        <label>Estado físico del bien
          <select ref={selector} value={inspeccion.estadoFisico} onChange={(e) => setInspeccion({
            ...inspeccion, estadoFisico: e.target.value as NuevaInspeccionDevolucion['estadoFisico'],
          })}>
            <option value="DISPONIBLE">Bueno</option><option value="DANADO">Dañado</option><option value="PERDIDO">Perdido</option>
          </select>
        </label>
        <label className="opcion-permiso"><input type="checkbox" checked={inspeccion.accesoriosFaltantes}
          onChange={(e) => setInspeccion({ ...inspeccion, accesoriosFaltantes: e.target.checked })} />
          Faltan accesorios o componentes
        </label>
        <label>Observaciones{requiereObservaciones ? ' (obligatorias)' : ''}
          <textarea rows={4} maxLength={1000} required={requiereObservaciones} value={inspeccion.observaciones}
            onChange={(e) => setInspeccion({ ...inspeccion, observaciones: e.target.value })}
            placeholder="Describe el estado del bien y sus accesorios" />
        </label>
        {inspeccion.accesoriosFaltantes && inspeccion.estadoFisico === 'DISPONIBLE' &&
          <p className="aviso">El ejemplar quedará en revisión y no estará disponible para préstamos.</p>}
        {error && <div className="aviso error-aviso" role="alert">{error}</div>}
        <div className="acciones"><button className="boton primario" type="submit">{enviando ? 'Registrando…' : 'Confirmar recepción'}</button>
          <button className="boton secundario" type="button" onClick={cancelar}>Cancelar</button>
        </div>
      </fieldset>
    </form>
  </section>
}
