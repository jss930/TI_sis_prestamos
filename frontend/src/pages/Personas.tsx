import { useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { NuevoUsuarioCuenta, PermisoOperativo, Usuario } from '../types'
import { etiquetaPerfil } from '../utils/format'

interface Props {
  usuarios: Usuario[]
  permisos: PermisoOperativo[]
  crearUsuario: (usuario: NuevoUsuarioCuenta) => Promise<void>
  validarUsuario: (id: string) => Promise<void>
  suspenderUsuario: (id: string, motivo: string) => Promise<void>
  reactivarUsuario: (id: string) => Promise<void>
  otorgarPermiso: (id: string, permiso: PermisoOperativo) => Promise<void>
  revocarPermiso: (id: string, permiso: PermisoOperativo) => Promise<void>
}

const PERMISOS: PermisoOperativo[] = [
  'REGISTRAR_PRESTAMO',
  'VALIDAR_USUARIO_PROVISIONAL',
  'GESTIONAR_CATALOGO',
  'GESTIONAR_SANCIONES',
  'GESTIONAR_USUARIOS',
]

export default function Personas({
  usuarios,
  permisos,
  crearUsuario,
  validarUsuario,
  suspenderUsuario,
  reactivarUsuario,
  otorgarPermiso,
  revocarPermiso,
}: Props) {
  const [busqueda, setBusqueda] = useState('')
  const [nuevo, setNuevo] = useState<NuevoUsuarioCuenta>({
    email: '',
    nombreCompleto: '',
    perfil: 'ALUMNO',
  })
  const [motivos, setMotivos] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)

  const puedeGestionar = permisos.includes('GESTIONAR_USUARIOS') || permisos.includes('VALIDAR_USUARIO_PROVISIONAL')

  const filtradas = usuarios.filter((usuario) =>
    `${usuario.nombreCompleto} ${usuario.email} ${usuario.perfil} ${usuario.estado} ${usuario.rolAcceso}`
      .toLowerCase()
      .includes(busqueda.toLowerCase()),
  )

  async function ejecutar(accion: () => Promise<void>) {
    setError(null)
    try {
      await accion()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo completar la operacion.')
    }
  }

  const columnas: Columna<Usuario>[] = [
    { clave: 'nombreCompleto', titulo: 'NOMBRE COMPLETO' },
    { clave: 'email', titulo: 'CORREO' },
    {
      clave: 'perfil',
      titulo: 'PERFIL',
      render: (usuario) => etiquetaPerfil(usuario.perfil),
    },
    {
      clave: 'rolAcceso',
      titulo: 'ROL',
      render: (usuario) => usuario.rolAcceso,
    },
    {
      clave: 'estado',
      titulo: 'CUENTA',
      render: (usuario) => <EtiquetaEstado estado={usuario.estado} />,
    },
    {
      clave: 'permisos',
      titulo: 'PERMISOS',
      render: (usuario) => (
        <span className="permisos-lista">
          {usuario.permisos.length
            ? usuario.permisos.map((permiso) => (
                <span className="chip" key={permiso}>
                  {permiso}
                </span>
              ))
            : '—'}
        </span>
      ),
    },
    {
      clave: 'id',
      titulo: 'ACCIONES',
      render: (usuario) => (
        <div className="acciones-fila">
          {usuario.estado === 'PROVISIONAL' && (
            <button
              className="boton secundario"
              onClick={() => void ejecutar(() => validarUsuario(usuario.id))}
              type="button"
            >
              Validar
            </button>
          )}
          {usuario.estado === 'VALIDADO' && (
            <>
              <input
                className="input-compacto"
                placeholder="Motivo"
                value={motivos[usuario.id] ?? ''}
                onChange={(e) =>
                  setMotivos((actual) => ({ ...actual, [usuario.id]: e.target.value }))
                }
              />
              <button
                className="boton secundario"
                onClick={() =>
                  void ejecutar(() => suspenderUsuario(usuario.id, motivos[usuario.id] ?? ''))
                }
                type="button"
              >
                Suspender
              </button>
            </>
          )}
          {usuario.estado === 'SUSPENDIDO' && (
            <button
              className="boton secundario"
              onClick={() => void ejecutar(() => reactivarUsuario(usuario.id))}
              type="button"
            >
              Reactivar
            </button>
          )}
          {puedeGestionar && (
            <select
              className="input-compacto"
              defaultValue=""
              onChange={(e) => {
                const permiso = e.target.value as PermisoOperativo
                if (!permiso) return
                void ejecutar(() =>
                  usuario.permisos.includes(permiso)
                    ? revocarPermiso(usuario.id, permiso)
                    : otorgarPermiso(usuario.id, permiso),
                )
                e.target.value = ''
              }}
            >
              <option value="">Permisos…</option>
              {PERMISOS.map((permiso) => (
                <option key={permiso} value={permiso}>
                  {usuario.permisos.includes(permiso) ? `Revocar ${permiso}` : `Otorgar ${permiso}`}
                </option>
              ))}
            </select>
          )}
        </div>
      ),
    },
  ]

  return (
    <section className="panel">
      <div className="panel-titulo">
        <div>
          <h2>Directorio institucional</h2>
          <p>{filtradas.length} personas registradas</p>
        </div>
        <input
          className="buscador"
          placeholder="Buscar por nombre, correo o perfil…"
          value={busqueda}
          onChange={(evento) => setBusqueda(evento.target.value)}
        />
      </div>

      {error && <div className="aviso error-aviso">{error}</div>}

      {puedeGestionar && (
        <form
          className="form-inline"
          onSubmit={(e) => {
            e.preventDefault()
            void ejecutar(async () => {
              await crearUsuario(nuevo)
              setNuevo({ email: '', nombreCompleto: '', perfil: 'ALUMNO' })
            })
          }}
        >
          <input
            placeholder="Correo"
            value={nuevo.email}
            onChange={(e) => setNuevo({ ...nuevo, email: e.target.value })}
            required
          />
          <input
            placeholder="Nombre completo"
            value={nuevo.nombreCompleto}
            onChange={(e) => setNuevo({ ...nuevo, nombreCompleto: e.target.value })}
            required
          />
          <select
            value={nuevo.perfil}
            onChange={(e) => setNuevo({ ...nuevo, perfil: e.target.value as NuevoUsuarioCuenta['perfil'] })}
          >
            <option value="ALUMNO">Alumno</option>
            <option value="DOCENTE">Docente</option>
            <option value="ADMINISTRATIVO">Administrativo</option>
          </select>
          <button className="boton primario" type="submit">
            Registrar cuenta
          </button>
        </form>
      )}

      <Tabla filas={filtradas} columnas={columnas} vacio="No hay personas que coincidan con la búsqueda" />
    </section>
  )
}