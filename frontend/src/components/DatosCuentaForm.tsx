import { useEffect, useState } from 'react'
import type { DatosCuentaPendiente, Usuario } from '../types'
import { mensajeDeError } from '../services/api'

export default function DatosCuentaForm({ usuario, guardar }: {
  usuario: Usuario; guardar: (datos: DatosCuentaPendiente) => Promise<void>
}) {
  const [datos, setDatos] = useState<DatosCuentaPendiente>({ email: usuario.email, nombreCompleto: usuario.nombreCompleto,
    perfil: usuario.perfil, codigoInstitucional: usuario.codigoInstitucional ?? '' })
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState(false)
  useEffect(() => {
    setDatos({ email: usuario.email, nombreCompleto: usuario.nombreCompleto, perfil: usuario.perfil, codigoInstitucional: usuario.codigoInstitucional ?? '' })
    setError(null); setExito(false)
  }, [usuario.id, usuario.email, usuario.nombreCompleto, usuario.perfil, usuario.codigoInstitucional])
  return <form className="formulario" aria-label="Datos para revisión de cuenta" onSubmit={(e) => {
    e.preventDefault()
    if (guardando) return
    setGuardando(true); setError(null); setExito(false)
    void guardar(datos).then(() => setExito(true)).catch((error: unknown) => setError(mensajeDeError(error)))
      .finally(() => setGuardando(false))
  }}>
    {error && <div className="aviso error-aviso" role="alert">{error}</div>}
    {exito && <div className="aviso correcto-aviso" role="status">Datos enviados.</div>}
    <fieldset disabled={guardando}>
      <legend>Datos personales</legend>
      <label>Nombre completo<input required maxLength={120} value={datos.nombreCompleto} onChange={(e) => setDatos({ ...datos, nombreCompleto: e.target.value })} /></label>
      <label>Correo institucional<input type="email" required value={datos.email} onChange={(e) => setDatos({ ...datos, email: e.target.value })} /></label>
      <label>Código institucional<input required minLength={4} maxLength={30} pattern="[A-Za-z0-9-]{4,30}"
        placeholder="Ej.: 20241234 o DOC-0123" value={datos.codigoInstitucional} onChange={(e) => setDatos({ ...datos, codigoInstitucional: e.target.value })} /></label>
      <label>Perfil<select value={datos.perfil} onChange={(e) => setDatos({ ...datos, perfil: e.target.value as DatosCuentaPendiente['perfil'] })}>
        <option value="ALUMNO">Alumno</option><option value="DOCENTE">Docente</option><option value="ADMINISTRATIVO">Administrativo</option>
      </select></label>
      <button className="boton primario" type="submit">{guardando ? 'Enviando…' : 'Guardar cambios'}</button>
    </fieldset>
  </form>
}
