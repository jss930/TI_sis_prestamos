export default function EtiquetaEstado({ estado }) {
  const clase = ['Vencido', 'Suspendido', 'Activa'].includes(estado) ? 'peligro' : estado === 'En espera' ? 'espera' : 'correcto'
  return <span className={`etiqueta ${clase}`}>{estado}</span>
}
