const URL_API = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function solicitar(ruta, opciones = {}) {
  const respuesta = await fetch(`${URL_API}${ruta}`, {
    headers: { 'Content-Type': 'application/json', ...opciones.headers },
    ...opciones,
  })

  const contenido = await respuesta.text()
  const datos = contenido ? JSON.parse(contenido) : null

  if (!respuesta.ok) {
    // La regla la decide el backend; React solo muestra el mensaje recibido.
    throw new Error(datos?.mensaje || 'No se pudo completar la solicitud')
  }
  return datos
}

export const api = {
  iniciarSesion: (credenciales) => solicitar('/auth/login', { method: 'POST', body: JSON.stringify(credenciales) }),
  listarPersonas: () => solicitar('/personas'),
  listarItems: () => solicitar('/items'),
  listarPrestamos: () => solicitar('/prestamos'),
  crearPrestamo: (prestamo) => solicitar('/prestamos', { method: 'POST', body: JSON.stringify(prestamo) }),
  devolverPrestamo: (id) => solicitar(`/prestamos/${id}/devolucion`, { method: 'POST' }),
  listarReservas: () => solicitar('/reservas'),
  crearReserva: (reserva) => solicitar('/reservas', { method: 'POST', body: JSON.stringify(reserva) }),
  listarSanciones: () => solicitar('/sanciones'),
}
