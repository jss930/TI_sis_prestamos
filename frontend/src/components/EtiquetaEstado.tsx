type ClaseEtiqueta = 'correcto' | 'peligro' | 'espera' | 'neutro'

const ESTADOS: Record<string, { texto: string; clase: ClaseEtiqueta }> = {
  ACTIVO: { texto: 'Activo', clase: 'correcto' },
  VENCIDO: { texto: 'Vencido', clase: 'peligro' },
  DEVUELTO: { texto: 'Devuelto', clase: 'neutro' },
  PERDIDO: { texto: 'Perdido', clase: 'peligro' },
  PROVISIONAL: { texto: 'Provisional', clase: 'espera' },
  VALIDADO: { texto: 'Validado', clase: 'correcto' },
  SUSPENDIDO: { texto: 'Suspendido', clase: 'peligro' },
  ACTIVA: { texto: 'Activa', clase: 'peligro' },
  CUMPLIDA: { texto: 'Cumplida', clase: 'correcto' },
  ANULADA: { texto: 'Anulada', clase: 'neutro' },
  DISPONIBLE: { texto: 'Disponible', clase: 'correcto' },
  PRESTADO: { texto: 'Prestado', clase: 'espera' },
  DANADO: { texto: 'Dañado', clase: 'peligro' },
  BAJA: { texto: 'Baja', clase: 'neutro' },
}

export default function EtiquetaEstado({ estado }: { estado: string }) {
  const config = ESTADOS[estado] ?? { texto: estado, clase: 'neutro' as const }
  return <span className={`etiqueta ${config.clase}`}>{config.texto}</span>
}
