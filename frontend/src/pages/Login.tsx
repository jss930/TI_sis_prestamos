import { useState, type FormEvent } from 'react'
import type { Sesion } from '../types'

interface Props {
  alIngresar: (sesion: Sesion) => void
  alVerCatalogo: () => void
}

export default function Login({ alIngresar, alVerCatalogo }: Props) {
  const [correo, setCorreo] = useState('encargado@unsa.edu.pe')
  const [clave, setClave] = useState('')

  function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const nombre = correo.split('@')[0]?.replace(/[._-]/g, ' ') || 'Personal bibliotecario'
    alIngresar({ nombre, email: correo, publica: false })
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
          <p>Ingresa con tu cuenta de personal para operar el sistema.</p>
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
              placeholder="••••••••"
              required
            />
          </label>
          <button className="boton primario" type="submit">
            Ingresar al sistema →
          </button>
          <button className="boton secundario boton-catalogo" type="button" onClick={alVerCatalogo}>
            Ver catálogo público
          </button>
          <small>
            Modo demo: la autenticación real todavía no está implementada. Cualquier contraseña permite
            continuar; no uses credenciales reales.
          </small>
        </form>
      </section>
    </main>
  )
}
