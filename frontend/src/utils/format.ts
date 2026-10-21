import type { CatalogoItem, Ejemplar, LugarUso, MotivoSancion, PerfilPrestatario, Usuario } from '../types'

export function fechaCorta(valor: string | null | undefined): string {
  if (!valor) return '—'
  const fecha = valor.length === 10 ? new Date(`${valor}T12:00:00`) : new Date(valor)
  if (Number.isNaN(fecha.getTime())) return valor
  return new Intl.DateTimeFormat('es-PE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(fecha)
}

export function dinero(monto: number, moneda: string): string {
  try {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: moneda || 'PEN',
    }).format(monto)
  } catch {
    return `${moneda} ${monto.toFixed(2)}`
  }
}

export function iniciales(nombre: string): string {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0]?.toUpperCase() ?? '')
    .join('')
}

export function nombreUsuario(usuarios: Usuario[], id: string): string {
  return usuarios.find((usuario) => usuario.id === id)?.nombreCompleto ?? id
}

export function tituloDeEjemplar(
  ejemplares: Ejemplar[],
  catalogo: CatalogoItem[],
  ejemplarId: string,
): string {
  const ejemplar = ejemplares.find((actual) => actual.id === ejemplarId)
  if (!ejemplar) return ejemplarId
  const item = catalogo.find((actual) => actual.id === ejemplar.catalogoItemId)
  return item ? `${item.titulo} · ${ejemplar.barcode}` : ejemplar.barcode
}

export function descripcionItem(item: CatalogoItem): string {
  const detalle = item.detalle
  if (item.tipo === 'LIBRO') {
    return (
      [detalle.autor, detalle.editorial, detalle.anioPublicacion?.toString()]
        .filter(Boolean)
        .join(' · ') || 'Sin datos bibliográficos'
    )
  }
  if (item.tipo === 'EQUIPO') {
    return [detalle.marca, detalle.modelo].filter(Boolean).join(' · ') || 'Sin datos del equipo'
  }
  return detalle.descripcion || 'Sin descripción'
}

export function etiquetaPerfil(perfil: PerfilPrestatario): string {
  return {
    ALUMNO: 'Alumno',
    DOCENTE: 'Docente',
    ADMINISTRATIVO: 'Administrativo',
  }[perfil]
}

export function etiquetaMotivo(motivo: MotivoSancion): string {
  return {
    RETRASO: 'Retraso',
    DANO: 'Daño',
    PERDIDA: 'Pérdida',
  }[motivo]
}

export function etiquetaLugar(lugar: LugarUso): string {
  return {
    EN_CAMPUS: 'En campus',
    DOMICILIO: 'Domicilio',
  }[lugar]
}
