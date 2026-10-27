import type { DatosBiblioteca, PermisoOperativo } from '../types'

export default function Administracion({ datos, permisos }: {
  datos: DatosBiblioteca; permisos: PermisoOperativo[]
}) {
  const resumen = [
    ['Catálogo', datos.catalogo.length], ['Ejemplares', datos.ejemplares.length],
    ['Personas', datos.usuarios.length], ['Préstamos', datos.prestamos.length],
    ['Sanciones', datos.sanciones.length], ['Reservas', datos.reservas.length],
  ]
  return <>
    {!permisos.includes('ADMINISTRAR_SISTEMA') && <div className="aviso">Esta vista requiere permiso de administración.</div>}
    <section className="admin-grid">
      <article className="panel"><h2>Datos del sistema</h2><dl className="resumen-datos">
        {resumen.map(([etiqueta, valor]) => <div key={etiqueta}><dt>{etiqueta}</dt><dd>{valor}</dd></div>)}
      </dl></article>
      <article className="panel"><h2>Políticas de préstamo</h2>
        <p>Se comprueban la habilitación de la cuenta, las sanciones, la disponibilidad y los límites de préstamo antes de cada entrega.</p>
        <p>Las reservas futuras requieren aprobación manual del administrador y no pueden superponerse sobre la misma copia.</p>
        <p>Los ejemplares dañados, perdidos o en revisión no están disponibles para nuevos préstamos.</p>
      </article>
    </section>
  </>
}
