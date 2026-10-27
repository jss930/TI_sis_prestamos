import type {
  CategoriaResponsable,
  DatosCuentaPendiente,
  CargoResponsable,
  ResumenResponsables,
  NuevaInspeccionDevolucion,
  InspeccionDevolucion,
  CatalogoItem,
  Ejemplar,
  EstadoFisico,
  NuevoEquipoCatalogo,
  NuevoEjemplar,
  NuevoLibroCatalogo,
  NuevoOtroCatalogo,
  NuevoPrestamo,
  NuevoUsuarioCuenta,
  PermisoOperativo,
  Prestamo,
  ProblemDetail,
  Reserva,

  Sancion,
  Usuario,
} from '../types'

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'
).replace(/\/+$/, '')

const MENSAJES_POR_CODIGO: Record<string, string> = {
  CODIGO_INSTITUCIONAL_REQUERIDO: 'Completa el código universitario o de personal antes de validar la cuenta.',
  CODIGO_INSTITUCIONAL_DUPLICADO: 'Ya existe una cuenta con ese código institucional.',
  CORRECCION_PENDIENTE: 'La persona debe corregir sus datos y enviarlos a revisión antes de validar su cuenta.',
  REVISION_REQUERIDA: 'Confirma que revisaste los datos de la cuenta.',
  CARGO_OCUPADO: 'Ese cargo del área ya está ocupado. Retira o cambia la asignación actual antes de reemplazarla.',
  AREA_NO_AUTORIZADA: 'No tienes una asignación vigente para atender esta área.',
  SOLICITUD_EXPIRADA: 'El plazo de recogida venció. El ejemplar se liberará automáticamente.',
  SOLICITUD_DUPLICADA: 'Ya tienes una solicitud activa para este artículo.',
  SANCION_ACTIVA: 'El usuario tiene una sanción activa. Debe cumplirla antes de un nuevo préstamo.',
  USUARIO_NO_HABILITADO: 'La cuenta del usuario no está validada para solicitar préstamos.',
  USUARIO_NO_VALIDADO: 'La cuenta debe estar validada para realizar esta operación.',
  AUTOPRESTAMO_NO_PERMITIDO: 'El operador no puede registrar un préstamo a su propia cuenta.',
  EJEMPLAR_NO_DISPONIBLE: 'El ejemplar ya no está disponible.',
  EJEMPLAR_YA_PRESTADO: 'El ejemplar ya tiene un préstamo activo.',
  PRESTAMO_VENCIDO_PENDIENTE: 'El usuario tiene un préstamo vencido sin devolver.',
  LIMITE_PRESTAMOS: 'El usuario alcanzó el máximo de préstamos simultáneos.',
  LIMITE_RESERVAS: 'El usuario alcanzó el máximo de reservas activas.',
  LIMITE_RENOVACIONES: 'El préstamo alcanzó el máximo de renovaciones permitidas.',
  DOMICILIO_NO_PERMITIDO: 'El perfil no puede llevar este tipo de bien a domicilio.',
  OPERADOR_NO_HABILITADO: 'El operador no tiene permiso para realizar esta operación.',
  ITEM_INACTIVO: 'El bien está fuera de circulación.',
  RECURSO_NO_ENCONTRADO: 'No se encontró el recurso solicitado.',
  VALIDACION: 'Revisa los campos del formulario.',
  PETICION_INVALIDA: 'La solicitud no se pudo interpretar.',
  ERROR_INTERNO: 'Ocurrió un error interno en el servidor.',
  SIN_CONEXION:
    'No se pudo conectar con la API. Verifica que el backend esté ejecutándose en http://localhost:8080.',
}

export class ApiError extends Error {
  readonly status: number
  readonly codigo: string
  readonly campos?: Record<string, string>

  constructor(status: number, codigo: string, mensaje: string, campos?: Record<string, string>) {
    super(mensaje)
    this.name = 'ApiError'
    this.status = status
    this.codigo = codigo
    this.campos = campos
  }
}

export function mensajeDeError(error: unknown): string {
  if (error instanceof ApiError) return error.message
  if (error instanceof Error) return error.message
  return 'No se pudo completar la solicitud.'
}

async function solicitar<T>(ruta: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = localStorage.getItem('biblioteca_token')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let respuesta: Response
  try {
    respuesta = await fetch(`${API_BASE_URL}${ruta}`, { ...init, headers })
  } catch {
    throw new ApiError(0, 'SIN_CONEXION', MENSAJES_POR_CODIGO.SIN_CONEXION)
  }

  const texto = await respuesta.text()
  let datos: unknown = null
  if (texto) {
    try {
      datos = JSON.parse(texto)
    } catch {
      datos = texto
    }
  }

  if (!respuesta.ok) {
    const problema =
      typeof datos === 'object' && datos !== null ? (datos as ProblemDetail) : null
    const codigo = problema?.codigo ?? `HTTP_${respuesta.status}`
    const detalle =
      problema?.detail ??
      (typeof datos === 'string' ? datos : 'La API rechazó la solicitud.')
    throw new ApiError(
      respuesta.status,
      codigo,
      MENSAJES_POR_CODIGO[codigo] ?? detalle,
      problema?.campos,
    )
  }

  return datos as T
}

export const api = {
  confirmarResponsables: (usuarioId: string, capacitacionVerificada: boolean, categorias: CategoriaResponsable[], cargos: Partial<Record<CategoriaResponsable, CargoResponsable>>) =>
    solicitar<ResumenResponsables>(`/responsables/${usuarioId}/asignaciones`, {
      method: 'PUT', body: JSON.stringify({ capacitacionVerificada, categorias, cargos }),
    }),
  getResponsables: () => solicitar<ResumenResponsables>('/responsables'),
  cambiarCapacitacion: (usuarioId: string, verificada: boolean) =>
    solicitar<ResumenResponsables>(`/responsables/${usuarioId}/capacitacion`, {
      method: 'PUT', body: JSON.stringify({ verificada }),
    }),
  asignarResponsable: (usuarioId: string, categoria: CategoriaResponsable, asignada: boolean) =>
    solicitar<ResumenResponsables>(`/responsables/${usuarioId}/categorias/${categoria}`, {
      method: 'PUT', body: JSON.stringify({ asignada }),
    }),
  getInspeccionesDevolucion: () => solicitar<InspeccionDevolucion[]>('/inspecciones-devolucion'),
  inspeccionarDevolucion: (id: string, inspeccion: NuevaInspeccionDevolucion) =>
    solicitar<InspeccionDevolucion>(`/prestamos/${id}/inspeccion-devolucion`, {
      method: 'POST', body: JSON.stringify(inspeccion),
    }),
  getCatalogo: () => solicitar<CatalogoItem[]>('/catalogo'),
  getUsuarios: () => solicitar<Usuario[]>('/usuarios'),
  getEjemplares: () => solicitar<Ejemplar[]>('/inventario/ejemplares'),
  getPrestamos: () => solicitar<Prestamo[]>('/prestamos'),
  getSanciones: () => solicitar<Sancion[]>('/sanciones'),
  crearPrestamo: (prestamo: NuevoPrestamo) =>
    solicitar<Prestamo>('/prestamos', {
      method: 'POST',
      body: JSON.stringify(prestamo),
    }),
  devolverPrestamo: (id: string, estadoFisico: EstadoFisico = 'DISPONIBLE') =>
    solicitar<Prestamo>(`/prestamos/${id}/devolucion`, {
      method: 'POST',
      body: JSON.stringify({ estadoFisico }),
    }),

  // Catalogo
  crearLibroCatalogo: (libro: NuevoLibroCatalogo) =>
    solicitar<CatalogoItem>('/catalogo/libros', {
      method: 'POST',
      body: JSON.stringify(libro),
    }),
  crearEquipoCatalogo: (equipo: NuevoEquipoCatalogo) =>
    solicitar<CatalogoItem>('/catalogo/equipos', {
      method: 'POST',
      body: JSON.stringify(equipo),
    }),
  crearOtroCatalogo: (otro: NuevoOtroCatalogo) =>
    solicitar<CatalogoItem>('/catalogo/otros', {
      method: 'POST',
      body: JSON.stringify(otro),
    }),
  actualizarTituloCatalogo: (id: string, titulo: string) =>
    solicitar<CatalogoItem>(`/catalogo/${id}/titulo`, {
      method: 'PATCH',
      body: JSON.stringify({ titulo }),
    }),
  activarCatalogo: (id: string) =>
    solicitar<CatalogoItem>(`/catalogo/${id}/activar`, { method: 'POST' }),
  desactivarCatalogo: (id: string) =>
    solicitar<CatalogoItem>(`/catalogo/${id}/desactivar`, { method: 'POST' }),

  // Usuarios
  registrarCuenta: (usuario: NuevoUsuarioCuenta) =>
    solicitar<Usuario>('/usuarios', {
      method: 'POST',
      body: JSON.stringify(usuario),
    }),
  validarUsuario: (id: string, operadorId: string) =>
    solicitar<Usuario>(`/usuarios/${id}/validar`, {
      method: 'POST',
      body: JSON.stringify({ operadorId, datosRevisados: true }),
    }),
  solicitarCorreccionCuenta: (id: string, observaciones: string) => solicitar<Usuario>(`/usuarios/${id}/solicitar-correccion`, {
    method: 'POST', body: JSON.stringify({ observaciones }),
  }),
  actualizarDatosCuenta: (id: string, datos: DatosCuentaPendiente) => solicitar<Usuario>(`/usuarios/${id}/datos-pendientes`, {
    method: 'PATCH', body: JSON.stringify(datos),
  }),
  suspenderUsuario: (id: string, operadorId: string, motivo: string) =>
    solicitar<Usuario>(`/usuarios/${id}/suspender`, {
      method: 'POST',
      body: JSON.stringify({ operadorId, motivo }),
    }),
  reactivarUsuario: (id: string, operadorId: string) =>
    solicitar<Usuario>(`/usuarios/${id}/reactivar`, {
      method: 'POST',
      body: JSON.stringify({ operadorId }),
    }),
  otorgarPermisoUsuario: (id: string, operadorId: string, permiso: PermisoOperativo) =>
    solicitar<Usuario>(`/usuarios/${id}/permisos`, {
      method: 'POST',
      body: JSON.stringify({ operadorId, permiso }),
    }),
  revocarPermisoUsuario: (id: string, permiso: PermisoOperativo, operadorId: string) =>
    solicitar<Usuario>(`/usuarios/${id}/permisos/${permiso}?operadorId=${operadorId}`, {
      method: 'DELETE',
    }),

  // Inventario
  crearEjemplar: (ejemplar: NuevoEjemplar) =>
    solicitar<Ejemplar>('/inventario/ejemplares', {
      method: 'POST',
      body: JSON.stringify(ejemplar),
    }),
  darDeBajaEjemplar: (id: string, motivo: string) =>
    solicitar<Ejemplar>(`/inventario/ejemplares/${id}/baja`, {
      method: 'POST',
      body: JSON.stringify({ motivo }),
    }),

  // Reservas
  login: (email: string, password: string) =>
    solicitar<{ token: string; usuario: Usuario }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  getReservas: (prestatarioId?: string, estado?: string) => {
    const params = new URLSearchParams()
    if (prestatarioId) params.set('prestatarioId', prestatarioId)
    if (estado) params.set('estado', estado)
    const qs = params.toString()
    return solicitar<Reserva[]>(`/reservas${qs ? `?${qs}` : ''}`)
  },
  solicitarReserva: (prestatarioId: string, catalogoItemId: string, lugarUso: 'EN_CAMPUS' | 'DOMICILIO' = 'EN_CAMPUS') =>
    solicitar<Reserva>('/reservas', {
      method: 'POST',
      body: JSON.stringify({ prestatarioId, catalogoItemId, lugarUso }),
    }),
  aceptarReserva: (id: string, operadorId: string, ejemplarId?: string) =>
    solicitar<Reserva>(`/reservas/${id}/aceptar`, {
      method: 'POST',
      body: JSON.stringify({ operadorId, ejemplarId }),
    }),
  entregarSolicitud: (id: string) => solicitar<Reserva>(`/reservas/${id}/entregar`, { method: 'POST' }),
  asignarAreaCatalogo: (id: string, area: string) => solicitar<void>(`/catalogo/${id}/area`, {
    method: 'PATCH', body: JSON.stringify({ area }),
  }),
  denegarReserva: (id: string, operadorId: string, motivo: string) =>
    solicitar<Reserva>(`/reservas/${id}/denegar`, {
      method: 'POST',
      body: JSON.stringify({ operadorId, motivo }),
    }),
  cancelarReserva: (id: string, prestatarioId: string) =>
    solicitar<Reserva>(`/reservas/${id}/cancelar`, {
      method: 'POST',
      body: JSON.stringify({ prestatarioId }),
    }),
}

export const API_BASE_URL_PUBLICA = API_BASE_URL
