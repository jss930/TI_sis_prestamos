import { useState, type FormEvent } from 'react'
import type { Sesion } from '../types'

interface Props {
  alIngresar: (sesion: Sesion) => void
  alVerCatalogo: () => void
}

const CUENTAS_DEMO = [
  { nombre: 'Elena Bibliotecaria', email: 'bibliotecario@biblioteca.pe', rol: 'Administrador' },
  { nombre: 'Luis Quispe', email: 'luis@universidad.edu.pe', rol: 'Estudiante' },
  { nombre: 'Ana Torres', email: 'ana@universidad.edu.pe', rol: 'Estudiante (provisional)' },
  { nombre: 'Rosa Docente', email: 'docente@universidad.edu.pe', rol: 'Docente' },
]

export default function Login({ alIngresar, alVerCatalogo }: Props) {
  const [correo, setCorreo] = useState('bibliotecario@biblioteca.pe')
  const [clave, setClave] = useState('')

  function ingresarConCorreo(email: string) {
    const nombre = email.split('@')[0]?.replace(/[._-]/g, ' ') || 'Personal bibliotecario'
    alIngresar({ nombre, email, publica: false })
  }

  function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    ingresarConCorreo(correo)
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
          <p>Ingresa con una cuenta demo para operar el sistema.</p>

          <div className="aviso" style={{ marginBottom: '1rem' }}>
            Modo demo: sin contraseña. La sesión se resuelve por el correo elegido.
          </div>

          <div className="cuentas-demo">
            {CUENTAS_DEMO.map((cuenta) => (
              <button
                key={cuenta.email}
                className="boton secundario"
                type="button"
                onClick={() => ingresarConCorreo(cuenta.email)}
              >
                {cuenta.nombre} — {cuenta.rol}
              </button>
            ))}
          </div>

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
            Contraseña (demo)
            <input
              type="password"
              value={clave}
              onChange={(evento) => setClave(evento.target.value)}
              placeholder="••••••••"
            />
          </label>
          <button className="boton primario" type="submit">
            Ingresar al sistema →
          </button>
          <button className="boton secundario boton-catalogo" type="button" onClick={alVerCatalogo}>
            Ver catálogo público
          </button>
        </form>
      </section>
    </main>
  )
}