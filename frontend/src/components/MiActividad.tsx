import type { ReactNode } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'

export default function MiActividad({ prestamos, reservas, historial }: { prestamos: ReactNode; reservas: ReactNode; historial: ReactNode }) {
  const [parametros] = useSearchParams()
  const location = useLocation()
  const tab = location.pathname === '/mis-reservas' ? 'reservas'
    : location.pathname === '/mis-prestamos' ? 'prestamos'
    : parametros.get('tab') || 'prestamos'
  return <>
    <nav className="pestanas no-imprimir" aria-label="Mi actividad">
      {[['prestamos', 'Préstamos activos'], ['reservas', 'Reservas'], ['historial', 'Mi historial y reporte']].map(([id, texto]) =>
        <Link key={id} className={tab === id ? 'activa' : ''} aria-current={tab === id ? 'page' : undefined}
          to={`/mi-actividad?tab=${id}`}>{texto}</Link>)}
    </nav>
    {tab === 'reservas' ? reservas : tab === 'historial' ? historial : prestamos}
  </>
}
