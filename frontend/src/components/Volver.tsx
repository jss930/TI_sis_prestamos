import { useNavigate } from 'react-router-dom'

export default function Volver({ a, texto = 'Volver' }: { a?: string; texto?: string }) {
  const navegar = useNavigate()
  return <button className="volver" type="button" onClick={() => a ? navegar(a) : navegar(-1)} aria-label={texto}>
    <span aria-hidden="true">←</span> {texto}
  </button>
}
