import { useState, type FormEvent } from 'react'
import type { Sesion } from '../types'
import { api, mensajeDeError } from '../services/api'

interface Props {
  alIngresar: (sesion: Sesion) => void
  alVerCatalogo: () => void
}

export default function Login({ alIngresar, alVerCatalogo }: Props) {
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(false)

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

  return (
    <main className="login">
      <section className="login-mensaje">
        <div className="sello">B</div>
        <p className="sobrelinea">UNIVERSIDAD NACIONAL DE SAN AGUSTÍN</p>
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
        <form onSubmit={enviar}>
          <p className="sobrelinea">PORTAL INSTITUCIONAL</p>
          <h2>Bienvenido</h2>
          <p>Ingresa con tu cuenta para operar el sistema.</p>

          {error && <div className="aviso error-aviso">{error}</div>}

          <label>
            Correo institucional
            <input
              type="email"
              value={correo}
              onChange={(evento) => setCorreo(evento.target.value)}
              required
            />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              value={clave}
              onChange={(evento) => setClave(evento.target.value)}
              required
            />
          </label>
          <button className="boton primario" type="submit" disabled={cargando}>
            {cargando ? 'Ingresando…' : 'Ingresar al sistema →'}
          </button>
          <button className="boton secundario" type="button" onClick={alVerCatalogo}>
            Ver catálogo público
          </button>
        </form>
      </section>
    </main>
  )
}
