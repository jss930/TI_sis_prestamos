import type { LugarUso, PerfilPrestatario, TipoItem } from '../types'

export const HORA = 60 * 60 * 1000
export const DIAS_DOMICILIO = {
  ALUMNO: { LIBRO: 3, EQUIPO: 0, OTRO: 0 },
  DOCENTE: { LIBRO: 5, EQUIPO: 3, OTRO: 0 },
  ADMINISTRATIVO: { LIBRO: 3, EQUIPO: 3, OTRO: 0 },
} as const

// Lima no tiene horario de verano. Los formularios usan siempre esta zona,
// independientemente de la zona configurada en el navegador.
export function valorLima(fecha: Date) {
  return new Date(fecha.getTime() - 5 * HORA).toISOString().slice(0, 16)
}

export function instanteLima(valor: string) {
  return new Date(`${valor.length === 16 ? `${valor}:00` : valor}-05:00`)
}

export function permiteDomicilio(perfil: PerfilPrestatario, tipo: TipoItem) {
  return DIAS_DOMICILIO[perfil][tipo] > 0
}

export function finMaximo(inicio: string, lugar: LugarUso, perfil: PerfilPrestatario, tipo: TipoItem) {
  if (!inicio || Number.isNaN(instanteLima(inicio).getTime())) return ''
  return lugar === 'EN_CAMPUS' ? `${inicio.slice(0, 10)}T20:00`
    : valorLima(new Date(instanteLima(inicio).getTime() + DIAS_DOMICILIO[perfil][tipo] * 24 * HORA))
}

export function descripcionPlazo(lugar: LugarUso, perfil: PerfilPrestatario, tipo: TipoItem) {
  return lugar === 'EN_CAMPUS' ? 'De 1 a 13 horas, el mismo día entre 07:00 y 20:00 (Lima).'
    : `De 1 hora a ${DIAS_DOMICILIO[perfil][tipo]} días exactos. Entrega y devolución entre 07:00 y 20:00 (Lima).`
}

export function validarPlazo(inicio: string, fin: string, lugar: LugarUso, perfil: PerfilPrestatario, tipo: TipoItem) {
  const duracion = instanteLima(fin).getTime() - instanteLima(inicio).getTime()
  if (!Number.isFinite(duracion)) throw new Error('Selecciona un horario válido.')
  if (lugar === 'DOMICILIO' && !permiteDomicilio(perfil, tipo)) throw new Error('Este bien solo se permite en campus para este perfil.')
  if (duracion < HORA) throw new Error('El plazo debe durar al menos una hora.')
  const maximo = lugar === 'EN_CAMPUS' ? 13 * HORA : DIAS_DOMICILIO[perfil][tipo] * 24 * HORA
  if (duracion > maximo) throw new Error(`El plazo máximo es de ${maximo / HORA} horas.`)
  if (lugar === 'EN_CAMPUS' && inicio.slice(0, 10) !== fin.slice(0, 10)) throw new Error('En campus la entrega y devolución deben ser el mismo día.')
  for (const valor of [inicio, fin]) {
    const hora = valor.slice(11)
    if (hora < '07:00' || hora > '20:00') throw new Error('El horario de atención es de 07:00 a 20:00 (Lima).')
  }
}

export function siguienteInicioReserva(ahora = new Date()) {
  let valor = valorLima(new Date(Math.ceil((ahora.getTime() + HORA / 2) / (HORA / 4)) * (HORA / 4)))
  if (valor.slice(11) < '07:00') valor = `${valor.slice(0, 10)}T07:00`
  if (valor.slice(11) > '19:00') {
    const siguiente = new Date(`${valor.slice(0, 10)}T12:00:00Z`)
    siguiente.setUTCDate(siguiente.getUTCDate() + 1)
    valor = `${siguiente.toISOString().slice(0, 10)}T07:00`
  }
  return valor
}
