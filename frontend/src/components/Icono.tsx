interface Props { nombre: string }
const trazos: Record<string, string> = {
  'mi-cuenta': 'M20 21v-2a7 7 0 0 0-14 0v2M17 7a5 5 0 1 1-10 0 5 5 0 0 1 10 0',
  resumen: 'M3 10 12 3l9 7M5 9v12h5v-7h4v7h5V9',
  personas: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M17 4a4 4 0 0 1 0 8M22 21v-2a4 4 0 0 0-3-3.87M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
  catalogo: 'M12 5v16M12 5C9 3 5 3 2 4v15c3-1 7-1 10 2 3-3 7-3 10-2V4c-3-1-7-1-10 1',
  prestamos: 'M4 7h16m-5-5 5 5-5 5M20 17H4m5-5-5 5 5 5',
  reservas: 'M8 3H5v18h14V3h-3M8 2h8v4H8zM8 11h8M8 15h5',
  sanciones: 'm12 3 10 18H2L12 3Zm0 6v5m0 3v1',
  administracion: 'M4 6h16M4 12h16M4 18h16M8 3v6M16 9v6M9 15v6',
  salir: 'M9 4H3v16h6M9 12h12m-5-5 5 5-5 5',
}
export default function Icono({ nombre }: Props) {
  const clave = nombre.replace('mis-', '')
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={trazos[clave] ?? trazos.catalogo} /></svg>
}
