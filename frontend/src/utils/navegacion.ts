import type { Pagina, PermisoOperativo } from '../types'

export function puedeVerPersonas(permisos: PermisoOperativo[]) {
  return permisos.some((p) => ['GESTIONAR_USUARIOS', 'VALIDAR_USUARIO_PROVISIONAL', 'GESTIONAR_SANCIONES', 'ADMINISTRAR_SISTEMA'].includes(p))
}

export function opcionesNavegacion(permisos: PermisoOperativo[], validada: boolean, publica: boolean): Pagina[] {
  if (publica || !validada) return ['catalogo']
  const opciones: Pagina[] = []
  if (permisos.length) opciones.push('resumen')
  opciones.push('catalogo')
  if (permisos.includes('ADMINISTRAR_SISTEMA')) opciones.push('reservas')
  if (permisos.includes('REGISTRAR_PRESTAMO') || permisos.includes('REGISTRAR_DEVOLUCION')) opciones.push('prestamos')
  if (puedeVerPersonas(permisos)) opciones.push('personas')
  if (permisos.includes('ADMINISTRAR_SISTEMA')) opciones.push('historial')
  opciones.push('actividad')
  return opciones
}
