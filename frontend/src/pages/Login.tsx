import { useState, type FormEvent } from 'react'
import type { NuevoUsuarioCuenta, Sesion } from '../types'
import { api, mensajeDeError } from '../services/api'

interface Props {
  alIngresar: (sesion: Sesion) => void
}

export default function Login({ alIngresar }: Props) {
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [mostrarClave, setMostrarClave] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)
  const [modoRegistro, setModoRegistro] = useState(false)
  const [registro, setRegistro] = useState<NuevoUsuarioCuenta>({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
  const [confirmacion, setConfirmacion] = useState('')

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setError(null)
    setCargando(true)
    try {
      const respuesta = await api.login(correo, clave)
      localStorage.setItem('biblioteca_token', respuesta.token)
      alIngresar({
        nombre: respuesta.usuario.nombreCompleto,
        email: respuesta.usuario.email,
        publica: false,
        id: respuesta.usuario.id,
        token: respuesta.token,
        permisos: respuesta.usuario.permisos as Sesion['permisos'],
        rolAcceso: respuesta.usuario.rolAcceso as Sesion['rolAcceso'],
      })
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setCargando(false)
    }
  }

  async function registrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setError(null)
    if (registro.password !== confirmacion) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setCargando(true)
    try {
      await api.registrarCuenta(registro)
      setCorreo(registro.email)
      setClave('')
      setRegistro({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
      setConfirmacion('')
      setMostrarClave(false)
      setModoRegistro(false)
      setError('Cuenta creada. Espera la validación del administrador para operar el sistema.')
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setCargando(false)
    }
  }

  return (
    <main className="login">
      <section className="login-mensaje">
        <div className="sello">B</div>
        <p className="sobrelinea">UNIVERSIDAD NACIONAL DE SAN AGUSTÍN</p>
        <p className="sobrelinea">ESCUELA PROFESIONAL DE CIENCIA DE LA COMPUTACIÓN</p>
        <h1>
          Los recursos que necesitas,
          <br />
          <em>cuando los necesitas.</em>
        </h1>
        <p>Gestiona préstamos de libros, equipos y material institucional desde un solo lugar.</p>
        <div className="beneficios">
          <span>✓ Catálogo actualizado</span>
          <span>✓ Préstamos y devoluciones</span>
          <span>✓ Sanciones visibles</span>
        </div>
      </section>
      <section className="login-panel">
        <form onSubmit={modoRegistro ? registrar : enviar}>
          <p className="sobrelinea">PORTAL INSTITUCIONAL</p>
          <h2>{modoRegistro ? 'Crear cuenta' : 'Bienvenido'}</h2>
          <p>{modoRegistro ? 'Regístrate para consultar el catálogo y solicitar préstamos.' : 'Ingresa con tu cuenta para operar el sistema.'}</p>

          {error && <div className="aviso error-aviso">{error}</div>}

          {modoRegistro ? (
            <>
              <label>Nombre completo<input type="text" value={registro.nombreCompleto} onChange={(e) => setRegistro({ ...registro, nombreCompleto: e.target.value })} required /></label>
              <label>Correo institucional<input type="email" value={registro.email} onChange={(e) => setRegistro({ ...registro, email: e.target.value })} required /></label>
              <label>Código universitario o de personal<input required minLength={4} maxLength={30} pattern="[A-Za-z0-9-]{4,30}" placeholder="Ej.: 20241234 o DOC-0123" value={registro.codigoInstitucional} onChange={(e) => setRegistro({ ...registro, codigoInstitucional: e.target.value })} /></label>
              <label>Tipo de usuario<select value={registro.perfil} onChange={(e) => setRegistro({ ...registro, perfil: e.target.value as NuevoUsuarioCuenta['perfil'] })}><option value="ALUMNO">Alumno</option><option value="DOCENTE">Docente</option><option value="ADMINISTRATIVO">Administrativo</option></select></label>
              <label>Contraseña<input id="registro-clave" type={mostrarClave ? 'text' : 'password'} autoComplete="new-password" autoCapitalize="none" spellCheck={false} minLength={8} value={registro.password} onChange={(e) => setRegistro({ ...registro, password: e.target.value })} required /></label>
              <label>Confirmar contraseña<input id="registro-confirmacion" type={mostrarClave ? 'text' : 'password'} autoComplete="new-password" autoCapitalize="none" spellCheck={false} minLength={8} value={confirmacion} onChange={(e) => setConfirmacion(e.target.value)} required /></label>
              <label className="mostrar-clave"><input type="checkbox" checked={mostrarClave}
                aria-controls="registro-clave registro-confirmacion" onChange={(e) => setMostrarClave(e.target.checked)} />Mostrar contraseña</label>
              <button className="boton primario" type="submit" disabled={cargando}>{cargando ? 'Creando cuenta…' : 'Crear cuenta →'}</button>
              <button className="boton secundario" type="button" onClick={() => { setModoRegistro(false); setMostrarClave(false); setError(null) }}>Ya tengo una cuenta</button>
            </>
          ) : (
            <>
              <label>Correo institucional<input type="email" value={correo} onChange={(evento) => setCorreo(evento.target.value)} required /></label>
              <label>Contraseña<input id="login-clave" type={mostrarClave ? 'text' : 'password'} autoComplete="current-password" autoCapitalize="none" spellCheck={false} value={clave} onChange={(evento) => setClave(evento.target.value)} required /></label>
              <label className="mostrar-clave"><input type="checkbox" checked={mostrarClave}
                aria-controls="login-clave" onChange={(e) => setMostrarClave(e.target.checked)} />Mostrar contraseña</label>
              <button className="boton primario" type="submit" disabled={cargando}>{cargando ? 'Ingresando…' : 'Ingresar al sistema →'}</button>
              <button className="boton secundario" type="button" onClick={() => { setModoRegistro(true); setMostrarClave(false); setError(null) }}>Crear una cuenta</button>
            </>
          )}
        </form>
      </section>
    </main>
  )
}
