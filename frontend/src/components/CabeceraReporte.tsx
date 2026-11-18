import type { ReactNode } from 'react'
import { descripcionPeriodo } from '../utils/reportes'
import { fechaHoraLima } from '../utils/format'

export default function CabeceraReporte({ titulo, desde, hasta, generadoEn, children }: {
  titulo: string; desde: string | null; hasta: string | null; generadoEn: string; children?: ReactNode
}) {
  return <header className="panel cabecera-reporte">
    <p className="sobrelinea">UNSA · Sistema de Préstamos Universitario</p>
    <h2>{titulo}</h2>
    {children}
    <p>Período consultado: {descripcionPeriodo(desde, hasta)}</p>
    <small>Generado: {fechaHoraLima(generadoEn)} (Lima). Los indicadores corresponden a todos los registros filtrados.</small>
  </header>
}
