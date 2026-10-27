export type Pagina =
  | 'resumen'
  | 'mi-cuenta'
  | 'personas'
  | 'catalogo'
  | 'mis-prestamos'
  | 'mis-reservas'
  | 'prestamos'
  | 'sanciones'
  | 'administracion'
  | 'reservas'

export interface Sesion {
  nombre: string
  email: string
  publica: boolean
  id?: string
  token?: string
  permisos?: PermisoOperativo[]
  rolAcceso?: RolAcceso
}

export type TipoItem = 'LIBRO' | 'EQUIPO' | 'OTRO'

export type EstadoCuenta = 'PROVISIONAL' | 'VALIDADO' | 'SUSPENDIDO'

export type PerfilPrestatario = 'ALUMNO' | 'DOCENTE' | 'ADMINISTRATIVO'

export type RolAcceso = 'ESTUDIANTE' | 'DOCENTE' | 'ADMINISTRATIVO' | 'ADMINISTRADOR'

export type PermisoOperativo =
  | 'ADMINISTRAR_SISTEMA'
  | 'REGISTRAR_PRESTAMO'
  | 'REGISTRAR_DEVOLUCION'
  | 'VALIDAR_USUARIO_PROVISIONAL'
  | 'GESTIONAR_CATALOGO'
  | 'GESTIONAR_SANCIONES'
  | 'GESTIONAR_USUARIOS'
  | 'GESTIONAR_RESERVAS'

export type EstadoFisico = 'DISPONIBLE' | 'RESERVADO' | 'PRESTADO' | 'DANADO' | 'PERDIDO' | 'BAJA' | 'EN_REVISION'

export type EstadoPrestamo = 'ACTIVO' | 'DEVUELTO' | 'PERDIDO'

export type LugarUso = 'EN_CAMPUS' | 'DOMICILIO'

export type MotivoSancion = 'RETRASO' | 'DANO' | 'PERDIDA'

export type EstadoSancion = 'ACTIVA' | 'CUMPLIDA' | 'ANULADA'

export type EstadoReserva = 'SOLICITADA' | 'APROBADA' | 'ATENDIDA' | 'EXPIRADA' | 'ACEPTADA' | 'DENEGADA' | 'CANCELADA'

export interface CatalogoDetalle {
  isbn: string | null
  autor: string | null
  editorial: string | null
  anioPublicacion: number | null
  marca: string | null
  modelo: string | null
  descripcion: string | null
}

export interface CatalogoItem {
  id: string
  titulo: string
  tipo: TipoItem
  activo: boolean
  area: CategoriaResponsable
  detalle: CatalogoDetalle
}

export interface Usuario {
  id: string
  email: string
  nombreCompleto: string
  estado: EstadoCuenta
  perfil: PerfilPrestatario
  permisos: PermisoOperativo[]
  fechaAlta: string
  fechaValidacion: string | null
  motivoSuspension: string | null
  rolAcceso: RolAcceso
  codigoInstitucional: string | null
  validadoPorId: string | null
  observacionesRevision: string | null
  revisadoPorId: string | null
  fechaRevision: string | null
  correccionPendiente: boolean
}

export interface Ejemplar {
  id: string
  catalogoItemId: string
  barcode: string
  estadoFisico: EstadoFisico
  sala: string
  estante: string
  notas: string | null
}

export interface Prestamo {
  id: string
  operadorId: string
  prestatarioId: string
  ejemplarId: string
  fechaInicio: string
  fechaVencimiento: string
  fechaDevolucion: string | null
  lugarUso: LugarUso
  estado: EstadoPrestamo
  renovaciones: number
  vencido: boolean
  diasRetraso: number
}

export interface Sancion {
  id: string
  usuarioId: string
  prestamoId: string | null
  motivo: MotivoSancion
  diasSuspension: number
  multa: number
  moneda: string
  fechaInicio: string
  fechaFin: string
  estado: EstadoSancion
  observaciones: string | null
}

export interface Reserva {
  id: string
  prestatarioId: string
  catalogoItemId: string
  estado: EstadoReserva
  operadorId: string | null
  prestamoId: string | null
  motivo: string | null
  fechaAlta: string
  fechaDecision: string | null
  ejemplarId: string | null
  fechaLimiteRetiro: string | null
  fechaEntrega: string | null
  entregadoPorId: string | null
  lugarUso: LugarUso
  inicioReserva: string | null
  finReserva: string | null
}

export interface NuevoEjemplar {
  catalogoItemId: string
  barcode: string
  sala: string
  estante: string
  notas?: string | null
}

export interface NuevoPrestamo {
  operadorId: string
  prestatarioId: string
  ejemplarId: string
  lugarUso: LugarUso
}

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  codigo?: string
  campos?: Record<string, string>
}

export interface NuevoLibroCatalogo {
  titulo: string
  isbn: string
  autor: string
  editorial: string
  anioPublicacion: number
}

export interface NuevoEquipoCatalogo {
  titulo: string
  marca: string
  modelo: string
}

export interface NuevoOtroCatalogo {
  titulo: string
  descripcion: string
}

export interface DatosCuentaPendiente {
  email: string
  nombreCompleto: string
  perfil: PerfilPrestatario
  codigoInstitucional: string
}

export interface NuevoUsuarioCuenta extends DatosCuentaPendiente {
  email: string
  nombreCompleto: string
  perfil: PerfilPrestatario
  password: string
}

export interface DatosBiblioteca {
  catalogo: CatalogoItem[]
  usuarios: Usuario[]
  ejemplares: Ejemplar[]
  prestamos: Prestamo[]
  sanciones: Sancion[]
  reservas: Reserva[]
}


export type CategoriaResponsable = 'BIBLIOTECA' | 'DEPORTES' | 'TECNOLOGIA' | 'COMPUTACION'
export interface NuevaInspeccionDevolucion {
  estadoFisico: 'DISPONIBLE' | 'DANADO' | 'PERDIDO'
  accesoriosFaltantes: boolean
  observaciones: string
}
export interface InspeccionDevolucion extends NuevaInspeccionDevolucion {
  prestamoId: string
  recibidoPorId: string
  fecha: string
  estadoResultante: EstadoFisico
}
