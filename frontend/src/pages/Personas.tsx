import { useEffect, useRef, useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import DatosCuentaForm from '../components/DatosCuentaForm'
import { api, mensajeDeError } from '../services/api'
import type { CategoriaResponsable, DatosCuentaPendiente, NuevoUsuarioCuenta, PermisoOperativo, Responsable, Usuario } from '../types'
import { etiquetaPerfil, fechaCorta, nombreUsuario } from '../utils/format'

interface Props {
  usuarios: Usuario[]
  permisos: PermisoOperativo[]
  operadorId: string | null
  crearUsuario: (usuario: NuevoUsuarioCuenta) => Promise<void>
  validarUsuario: (id: string) => Promise<void>
  solicitarCorreccion: (id: string, observaciones: string) => Promise<void>
  actualizarDatos: (id: string, datos: DatosCuentaPendiente) => Promise<void>
  suspenderUsuario: (id: string, motivo: string) => Promise<void>
  reactivarUsuario: (id: string) => Promise<void>
  gestionarResponsables: (usuarioId?: string) => void
}

const AREAS: Record<CategoriaResponsable, string> = {
  BIBLIOTECA: 'Biblioteca', DEPORTES: 'Deportes', TECNOLOGIA: 'Tecnología', COMPUTACION: 'Computación y periféricos',
}
export default function Personas({ usuarios, permisos, operadorId, crearUsuario, validarUsuario,
  suspenderUsuario, reactivarUsuario, gestionarResponsables, solicitarCorreccion, actualizarDatos }: Props) {
  const [mostrarRegistro, setMostrarRegistro] = useState(false)
  const [mostrarSuspension, setMostrarSuspension] = useState(false)
  const [filtro, setFiltro] = useState('TODAS')
  const [observaciones, setObservaciones] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [nuevo, setNuevo] = useState<NuevoUsuarioCuenta>({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
  const [seleccionadaId, setSeleccionadaId] = useState<string | null>(null)
  const [revisionDetalle, setRevisionDetalle] = useState(0)
  const detalleRef = useRef<HTMLHeadingElement>(null)
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [exito, setExito] = useState<string | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [responsables, setResponsables] = useState<Responsable[] | null>(null)
  const [errorResponsables, setErrorResponsables] = useState<string | null>(null)
  const [recarga, setRecarga] = useState(0)
  const puedeAdministrar = permisos.includes('ADMINISTRAR_SISTEMA')
  const puedeValidar = puedeAdministrar || permisos.includes('VALIDAR_USUARIO_PROVISIONAL')
  const puedeGestionar = permisos.includes('GESTIONAR_USUARIOS') || permisos.includes('VALIDAR_USUARIO_PROVISIONAL')
  const seleccionada = usuarios.find((usuario) => usuario.id === seleccionadaId)

  useEffect(() => {
    let vigente = true
    if (!puedeAdministrar) {
      setResponsables(null)
      setErrorResponsables(null)
      return
    }
    setErrorResponsables(null)
    void api.getResponsables().then((datos) => { if (vigente) setResponsables(datos.responsables) })
      .catch((e: unknown) => { if (vigente) setErrorResponsables(mensajeDeError(e)) })
    return () => { vigente = false }
  }, [usuarios, puedeAdministrar, recarga])

  useEffect(() => {
    if (!seleccionadaId || !detalleRef.current) return
    detalleRef.current.focus({ preventScroll: true })
    detalleRef.current.scrollIntoView({
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start',
    })
  }, [seleccionadaId, revisionDetalle])

  function funciones(usuario: Usuario): string[] {
    const resultado: string[] = []
    if (usuario.rolAcceso === 'ADMINISTRADOR') resultado.push('Administrador')
    const responsabilidad = responsables?.find((r) => r.usuarioId === usuario.id)
    if (responsabilidad) {
      for (const cargo of ['PRINCIPAL', 'SUPLENTE'] as const) {
        const areas = responsabilidad.categorias.filter((area) => responsabilidad.cargos[area] === cargo)
        if (areas.length) resultado.push(`${cargo === 'PRINCIPAL' ? 'Principal' : 'Suplente'} de ${areas.map((area) => AREAS[area]).join(', ')}`)
      }
    }
    if (!puedeAdministrar) return resultado.length ? resultado : ['Usuario']
    if (responsables === null) return resultado.length ? resultado : [errorResponsables ? 'Áreas no disponibles' : 'Cargando áreas…']
    return resultado.length ? resultado : ['Usuario']
  }

  const consulta = busqueda.trim().toLocaleLowerCase('es')
  const filtradas = usuarios.filter((usuario) =>
    (filtro === 'TODAS' || (filtro === 'RESPONSABLES' ? responsables?.some((r) => r.usuarioId === usuario.id && r.categorias.length > 0) : usuario.estado === filtro)) &&
    `${usuario.nombreCompleto} ${usuario.email} ${usuario.codigoInstitucional ?? ''} ${etiquetaPerfil(usuario.perfil)} ${usuario.estado} ${funciones(usuario).join(' ')}`
      .toLocaleLowerCase('es').includes(consulta),
  )
  if (filtro === 'PROVISIONAL') filtradas.sort((a, b) => a.fechaAlta.localeCompare(b.fechaAlta))
  const filtros = [
    { id: 'TODAS', texto: 'Todas', cantidad: usuarios.length },
    { id: 'PROVISIONAL', texto: 'Por validar', cantidad: usuarios.filter((u) => u.estado === 'PROVISIONAL').length },
    ...(puedeAdministrar ? [{ id: 'RESPONSABLES', texto: 'Responsables', cantidad: responsables === null ? '…' : responsables.filter((r) => r.categorias.length > 0).length }] : []),
    { id: 'VALIDADO', texto: 'Validadas', cantidad: usuarios.filter((u) => u.estado === 'VALIDADO').length },
    { id: 'SUSPENDIDO', texto: 'Suspendidas', cantidad: usuarios.filter((u) => u.estado === 'SUSPENDIDO').length },
  ]

  async function ejecutar(accion: () => Promise<void>, mensaje = 'Cambios guardados correctamente.') {
    if (procesando) return
    setProcesando(true)
    setError(null)
    setExito(null)
    try {
      await accion()
      setExito(mensaje)
    } catch (e) { setError(mensajeDeError(e)) }
    finally { setProcesando(false) }
  }

  const columnas: Columna<Usuario>[] = [
    { clave: 'persona', titulo: 'PERSONA', render: (usuario) => <div className="persona-identidad">
      <strong>{usuario.nombreCompleto}</strong><small>{usuario.email}</small>
    </div> },
    { clave: 'perfil', titulo: 'PERFIL', render: (usuario) => etiquetaPerfil(usuario.perfil) },
    { clave: 'estado', titulo: 'CUENTA', render: (usuario) => <div className="persona-funciones"><EtiquetaEstado estado={usuario.estado} />
      {usuario.estado === 'PROVISIONAL' && usuario.correccionPendiente && <small>Datos por corregir</small>}
    </div> },
    { clave: 'responsabilidades', titulo: 'FUNCIÓN Y ÁREAS', render: (usuario) => <div className="persona-funciones">
      {funciones(usuario).map((funcion) => <span key={funcion}>{funcion}</span>)}
    </div> },
    { clave: 'acciones', titulo: 'ACCIÓN', render: (usuario) => <div className="acciones-fila">
      {usuario.estado === 'PROVISIONAL' && puedeValidar && <button className="boton primario" type="button"
        aria-label={`Validar cuenta de ${usuario.nombreCompleto}`}
        title={!usuario.codigoInstitucional ? 'Completa el código institucional en Ver ficha' : usuario.correccionPendiente ? 'Espera los datos corregidos' : undefined}
        disabled={procesando || !usuario.codigoInstitucional || usuario.correccionPendiente || usuario.id === operadorId}
        onClick={() => {
          setSeleccionadaId(null)
          void ejecutar(() => validarUsuario(usuario.id), `Cuenta de ${usuario.nombreCompleto} validada.`)
        }}>Validar cuenta</button>}
      <button className="boton secundario" type="button"
      aria-label={`Ver ficha de ${usuario.nombreCompleto}`} disabled={procesando} onClick={() => {
        setSeleccionadaId(usuario.id)
        setRevisionDetalle((revision) => revision + 1)
        setMotivo('')
        setObservaciones('')
        setMostrarSuspension(false)
        setError(null)
        setExito(null)
      }}>Ver ficha</button>
    </div> },
  ]

  return <>
    <section className="panel">
      <div className="panel-titulo personas-titulo">
        <div><h2>Personas</h2><p>{usuarios.length} cuentas registradas</p></div>
        <div className="acciones-fila">
          {puedeAdministrar && <button className="boton secundario" type="button" disabled={procesando}
            onClick={() => gestionarResponsables()}>Gestionar responsables</button>}
          {puedeGestionar && <button className="boton primario" type="button" aria-expanded={mostrarRegistro}
            disabled={procesando} onClick={() => {
              if (mostrarRegistro) setNuevo({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
              setMostrarRegistro((actual) => !actual)
              setSeleccionadaId(null)
              setError(null)
              setExito(null)
            }}>
            {mostrarRegistro ? 'Cerrar registro' : '+ Nueva persona'}
          </button>}
        </div>
      </div>
      <div className="filtros" aria-label="Filtrar por estado de cuenta">
        {filtros.map((opcion) => <button type="button" key={opcion.id} className={filtro === opcion.id ? 'activo' : ''}
          aria-pressed={filtro === opcion.id} onClick={() => setFiltro(opcion.id)}>{opcion.texto} ({opcion.cantidad})</button>)}
      </div>
      <div className="personas-busqueda">
        <label className="personas-buscador">Buscar persona
          <input className="buscador" type="search" placeholder="Nombre, correo o código…" value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)} />
        </label>
        <span>{filtradas.length} {filtradas.length === 1 ? 'resultado' : 'resultados'}</span>
      </div>
      {errorResponsables && <div className="aviso error-aviso" role="alert">
        No se pudieron actualizar las responsabilidades: {errorResponsables}
        <button className="enlace" type="button" onClick={() => setRecarga((valor) => valor + 1)}>Reintentar</button>
      </div>}
      {!seleccionada && error && <div className="aviso error-aviso" role="alert">{error}</div>}
      {!seleccionada && exito && <div className="aviso correcto-aviso" role="status">{exito}</div>}
      {puedeGestionar && mostrarRegistro && <form className="formulario persona-registro" aria-label="Registrar nueva cuenta" onSubmit={(e) => {
        e.preventDefault()
        void ejecutar(async () => {
          await crearUsuario(nuevo)
          setNuevo({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
          setMostrarRegistro(false)
        }, 'Cuenta creada. Ya puedes validarla desde la lista.')
      }}>
        <h3>Nueva persona</h3>
        <fieldset disabled={procesando}>
          <legend>Datos de la cuenta</legend>
          <div className="persona-registro-campos">
            <label>Nombre completo<input required maxLength={120} placeholder="Nombres y apellidos" value={nuevo.nombreCompleto}
              onChange={(e) => setNuevo({ ...nuevo, nombreCompleto: e.target.value })} /></label>
            <label>Correo institucional<input required type="email" placeholder="nombre@unsa.edu.pe" value={nuevo.email}
              onChange={(e) => setNuevo({ ...nuevo, email: e.target.value })} /></label>
            <label>Código universitario o de personal<input required minLength={4} maxLength={30} pattern="[A-Za-z0-9-]{4,30}" placeholder="Ej.: 20241234"
              value={nuevo.codigoInstitucional} onChange={(e) => setNuevo({ ...nuevo, codigoInstitucional: e.target.value })} /></label>
            <label>Perfil<select value={nuevo.perfil} onChange={(e) => setNuevo({ ...nuevo, perfil: e.target.value as NuevoUsuarioCuenta['perfil'] })}>
              <option value="ALUMNO">Alumno</option><option value="DOCENTE">Docente</option><option value="ADMINISTRATIVO">Administrativo</option>
            </select></label>
            <label>Contraseña inicial<input required type="password" minLength={8} autoComplete="new-password" placeholder="Mínimo 8 caracteres"
              value={nuevo.password} onChange={(e) => setNuevo({ ...nuevo, password: e.target.value })} /></label>
          </div>
          <div className="acciones-fila">
            <button className="boton primario" type="submit">{procesando ? 'Guardando…' : 'Crear cuenta'}</button>
            <button className="boton secundario" type="button" onClick={() => {
              setMostrarRegistro(false)
              setNuevo({ email: '', nombreCompleto: '', perfil: 'ALUMNO', password: '', codigoInstitucional: '' })
            }}>Cancelar</button>
          </div>
        </fieldset>
      </form>}
      <Tabla filas={filtradas} columnas={filtro === 'PROVISIONAL' ? columnas.filter((columna) => columna.clave !== 'responsabilidades') : columnas} vacio="No hay personas que coincidan con la búsqueda" />
    </section>
    {seleccionada && <section className="panel formulario detalle-persona" aria-labelledby="detalle-persona-titulo">
      <div className="panel-titulo">
        <div><h2 id="detalle-persona-titulo" ref={detalleRef} tabIndex={-1}>{seleccionada.nombreCompleto}</h2><p>{seleccionada.email}</p>
          {seleccionada.estado === 'PROVISIONAL' && <EtiquetaEstado estado={seleccionada.estado} />}
        </div>
        <button className="boton secundario" type="button" disabled={procesando} onClick={() => setSeleccionadaId(null)}>Cerrar ficha</button>
      </div>
      {error && <div className="aviso error-aviso" role="alert">{error}</div>}
      {exito && <div className="aviso correcto-aviso" role="status">{exito}</div>}
      <dl className="detalle-solicitud">
        <div><dt>Código institucional</dt><dd>{seleccionada.codigoInstitucional ?? 'Pendiente de completar'}</dd></div>
        <div><dt>Perfil</dt><dd>{etiquetaPerfil(seleccionada.perfil)}</dd></div>
        {seleccionada.estado !== 'PROVISIONAL' && <>
          <div><dt>Estado</dt><dd><EtiquetaEstado estado={seleccionada.estado} /></dd></div>
          <div><dt>Responsabilidades</dt><dd className="persona-funciones">{funciones(seleccionada).map((funcion) => <span key={funcion}>{funcion}</span>)}</dd></div>
        </>}
        {seleccionada.motivoSuspension && <div><dt>Motivo de suspensión</dt><dd>{seleccionada.motivoSuspension}</dd></div>}
      </dl>
      {puedeAdministrar && seleccionada.estado !== 'PROVISIONAL' && <div className="persona-accesos">
        <div><strong>{seleccionada.rolAcceso === 'ADMINISTRADOR' ? 'Administración general' : responsables?.some((r) => r.usuarioId === seleccionada.id && r.categorias.length) ? 'Responsable de área' : 'Cuenta de usuario'}</strong>
          <p>{seleccionada.estado === 'SUSPENDIDO' ? 'Sus operaciones están bloqueadas mientras la cuenta esté suspendida.' : seleccionada.rolAcceso === 'ADMINISTRADOR' ? 'Gestiona el sistema y sus áreas.' : responsables?.some((r) => r.usuarioId === seleccionada.id && r.categorias.length)
            ? 'Sus accesos se actualizan al asignar o retirar responsabilidades.' : 'Consulta el catálogo y gestiona sus propias solicitudes.'}</p>
        </div>
        {seleccionada.rolAcceso !== 'ADMINISTRADOR' && <button className="boton secundario" type="button" disabled={procesando}
          onClick={() => gestionarResponsables(seleccionada.id)}>Gestionar áreas</button>}
      </div>}

      {seleccionada.estado === 'PROVISIONAL' && puedeValidar && <fieldset disabled={procesando}>
        <legend>Cuenta</legend>
        <button className="boton primario" type="button"
          disabled={!seleccionada.codigoInstitucional || seleccionada.correccionPendiente || seleccionada.id === operadorId}
          onClick={() => void ejecutar(() => validarUsuario(seleccionada.id), 'Cuenta validada.')}>Validar cuenta</button>
        {!seleccionada.codigoInstitucional && <p>Falta completar el código institucional.</p>}
        {seleccionada.correccionPendiente && <p>Esperando los datos corregidos.</p>}
        <details className="permisos-avanzados"><summary>Más opciones</summary>
          {seleccionada.observacionesRevision && <div className="aviso" role="status">
            <strong>{seleccionada.correccionPendiente ? 'Correcciones solicitadas' : 'Observaciones de la revisión anterior'}</strong>
            <p>{seleccionada.observacionesRevision}</p>
            {seleccionada.revisadoPorId && <p>Revisó: {nombreUsuario(usuarios, seleccionada.revisadoPorId)}{seleccionada.fechaRevision ? ` · ${new Date(seleccionada.fechaRevision).toLocaleString('es-PE')}` : ''}</p>}
            {!seleccionada.correccionPendiente && seleccionada.estado === 'PROVISIONAL' && <p>La persona envió sus datos. Pendiente de nueva revisión.</p>}
          </div>}

          <DatosCuentaForm usuario={seleccionada} guardar={(datos) => actualizarDatos(seleccionada.id, datos)} />
          <label>Observaciones
            <textarea rows={3} maxLength={1000} value={observaciones} placeholder="Indica qué dato debe corregirse."
              onChange={(e) => setObservaciones(e.target.value)} />
          </label>
          <button className="boton secundario" type="button" disabled={!observaciones.trim() || seleccionada.id === operadorId}
            onClick={() => void ejecutar(() => solicitarCorreccion(seleccionada.id, observaciones.trim()), 'Corrección solicitada.')}>Solicitar corrección</button>
        </details>
      </fieldset>}
      {seleccionada.estado !== 'PROVISIONAL' && puedeGestionar && <fieldset disabled={procesando}>
        <legend>Estado de la cuenta</legend>
        {seleccionada.estado === 'VALIDADO' && <>
          <button className="boton secundario" type="button" aria-expanded={mostrarSuspension}
            onClick={() => setMostrarSuspension((actual) => !actual)}>{mostrarSuspension ? 'Cancelar suspensión' : 'Suspender cuenta'}</button>
          {mostrarSuspension && <div className="persona-suspension">
            <label>Motivo<input placeholder="Indica el motivo de suspensión" value={motivo} onChange={(e) => setMotivo(e.target.value)} /></label>
            <button className="boton secundario" type="button" disabled={!motivo.trim()}
              onClick={() => void ejecutar(() => suspenderUsuario(seleccionada.id, motivo.trim()), 'Cuenta suspendida correctamente.')}>Confirmar suspensión</button>
          </div>}
        </>}
        {seleccionada.estado === 'SUSPENDIDO' && <button className="boton primario" type="button"
          onClick={() => void ejecutar(() => reactivarUsuario(seleccionada.id), 'Cuenta reactivada correctamente.')}>Reactivar cuenta</button>}
      </fieldset>}
      <details key={seleccionada.id} className="persona-historial">
        <summary>Historial</summary>
        {seleccionada.estado !== 'PROVISIONAL' && seleccionada.observacionesRevision && <p>{seleccionada.observacionesRevision}</p>}
        <dl className="detalle-solicitud">
          <div><dt>Registrada</dt><dd>{fechaCorta(seleccionada.fechaAlta)}</dd></div>
          {seleccionada.fechaValidacion && <div><dt>Validada</dt><dd>{new Date(seleccionada.fechaValidacion).toLocaleString('es-PE')}</dd></div>}
          {seleccionada.fechaValidacion && <div><dt>Validada por</dt><dd>{seleccionada.validadoPorId ? nombreUsuario(usuarios, seleccionada.validadoPorId) : 'Sin registro anterior'}</dd></div>}
        </dl>
      </details>
    </section>}
  </>
}
