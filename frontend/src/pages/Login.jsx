import { useState } from 'react'
export default function Login({ alIngresar }) {
  const [correo, setCorreo] = useState('encargado@unsa.edu.pe')
  const [clave, setClave] = useState('')
  const [cargando, setCargando] = useState(false)
  function enviar(evento) { evento.preventDefault(); setCargando(true); setTimeout(() => { setCargando(false); alIngresar({ nombre: 'María Elena', correo }) }, 450) }
  return <main className="login"><section className="login-mensaje"><div className="sello">B</div><p className="sobrelinea">UNIVERSIDAD NACIONAL DE SAN AGUSTÍN</p><h1>Los recursos que necesitas,<br/><em>cuando los necesitas.</em></h1><p>Gestiona préstamos de libros, equipos y material deportivo desde un solo lugar.</p><div className="beneficios"><span>✓ Catálogo actualizado</span><span>✓ Reservas en línea</span><span>✓ Historial transparente</span></div></section><section className="login-panel"><form onSubmit={enviar}><p className="sobrelinea">PORTAL INSTITUCIONAL</p><h2>Bienvenido</h2><p>Ingresa tus credenciales para continuar.</p><label>Correo institucional<input type="email" value={correo} onChange={e => setCorreo(e.target.value)} required /></label><label>Contraseña<input type="password" value={clave} onChange={e => setClave(e.target.value)} placeholder="••••••••" required /></label><button className="boton primario" disabled={cargando}>{cargando ? 'Ingresando…' : 'Ingresar al sistema →'}</button><small>Demo: escribe cualquier contraseña</small></form></section></main>
}
