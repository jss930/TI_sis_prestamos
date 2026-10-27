import DatosCuentaForm from '../components/DatosCuentaForm'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { DatosCuentaPendiente, Usuario } from '../types'
import { etiquetaPerfil } from '../utils/format'

export default function MiCuenta({ usuario, actualizar }: {
  usuario: Usuario | null; actualizar: (id: string, datos: DatosCuentaPendiente) => Promise<void>
}) {
  if (!usuario) return <section className="panel"><p>No se pudieron cargar los datos de tu cuenta.</p></section>
  return <section className="panel formulario">
    <h2>Mi cuenta</h2>
    <EtiquetaEstado estado={usuario.estado} />
    <p>{usuario.nombreCompleto} · {etiquetaPerfil(usuario.perfil)}</p>
    {usuario.estado === 'PROVISIONAL' && <p>Podrás solicitar préstamos cuando se valide.</p>}
    {usuario.observacionesRevision && <div className="aviso" role="status">
      <strong>{usuario.correccionPendiente ? 'Datos por corregir' : 'Observaciones'}</strong>
      <p>{usuario.observacionesRevision}</p>
      {!usuario.correccionPendiente && usuario.estado === 'PROVISIONAL' && <p>Datos enviados.</p>}
    </div>}
    <dl className="detalle-solicitud">
      <div><dt>Correo</dt><dd>{usuario.email}</dd></div><div><dt>Código institucional</dt><dd>{usuario.codigoInstitucional ?? 'No registrado'}</dd></div>
      {usuario.fechaValidacion && <div><dt>Validada</dt><dd>{new Date(usuario.fechaValidacion).toLocaleString('es-PE')}</dd></div>}
      {usuario.motivoSuspension && <div><dt>Motivo de suspensión</dt><dd>{usuario.motivoSuspension}</dd></div>}
    </dl>
    {usuario.estado === 'PROVISIONAL' && <details key={usuario.id} className="persona-historial">
      <summary>Editar datos</summary>
      <DatosCuentaForm usuario={usuario} guardar={(datos) => actualizar(usuario.id, datos)} />
    </details>}
  </section>
}
