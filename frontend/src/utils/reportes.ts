export function descripcionPeriodo(desde: string | null, hasta: string | null) {
  const fecha = (valor: string) => valor.split('-').reverse().join('/')
  if (!desde && !hasta) return 'Todo el historial disponible'
  if (desde && hasta) return `Del ${fecha(desde)} al ${fecha(hasta)} (incluidos)`
  return desde ? `Desde el ${fecha(desde)}` : `Hasta el ${fecha(hasta!)} (incluido)`
}

export function unirRegistros<T extends { id: string }>(anteriores: T[], nuevos: T[]): T[] {
  const registros = new Map(anteriores.map((r) => [r.id, r]))
  for (const registro of nuevos) registros.set(registro.id, registro)
  return [...registros.values()]
}
