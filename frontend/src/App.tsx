import { useCallback, useEffect, useState } from 'react'
import BarraLateral from './components/BarraLateral'
import Encabezado from './components/Encabezado'
import Login from './pages/Login'
import Resumen from './pages/Resumen'
import Personas from './pages/Personas'
import Catalogo from './pages/Catalogo'
import MisPrestamos from './pages/MisPrestamos'
import Prestamos from './pages/Prestamos'
import Sanciones from './pages/Sanciones'
import Administracion from './pages/Administracion'
import { OPERADOR_DEMO_ID } from './constants'
import { api, mensajeDeError } from './services/api'
import type {
  DatosBiblioteca,
  EstadoFisico,
  NuevoEquipoCatalogo,
  NuevoLibroCatalogo,
  NuevoOtroCatalogo,
  NuevoPrestamo,
  NuevoUsuarioCuenta,
  Pagina,
  PermisoOperativo,
  Sesion,
} from './types'

const DATOS_VACIOS: DatosBiblioteca = {
  catalogo: [],
  usuarios: [],
  ejemplares: [],
  prestamos: [],
  sanciones: [],
}

export default function App() {
  const [sesion, setSesion] = useState<Sesion | null>(null)
  const [pagina, setPagina] = useState<Pagina>('resumen')
  const [datos, setDatos] = useState<DatosBiblioteca>(DATOS_VACIOS)
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  const cargarDatos = useCallback(async (silencioso = false) => {
    if (!silencioso) setCargando(true)
    setErrorCarga(null)
    try {
      const [catalogo, usuarios, ejemplares, prestamos, sanciones] = await Promise.all([
        api.getCatalogo(),
        api.getUsuarios(),
        api.getEjemplares(),
        api.getPrestamos(),
        api.getSanciones(),
      ])
      setDatos({ catalogo, usuarios, ejemplares, prestamos, sanciones })
    } catch (error) {
      setErrorCarga(mensajeDeError(error))
    } finally {
      if (!silencioso) setCargando(false)
    }
  }, [])

  useEffect(() => {
    void cargarDatos()
  }, [cargarDatos])

  const crearPrestamo = useCallback(
    async (nuevoPrestamo: NuevoPrestamo) => {
      await api.crearPrestamo(nuevoPrestamo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  const devolverPrestamo = useCallback(
    async (id: string, estadoFisico?: EstadoFisico) => {
      await api.devolverPrestamo(id, estadoFisico)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  const usuarioActual = sesion && !sesion.publica
    ? datos.usuarios.find((usuario) => usuario.email === sesion.email) ?? null
    : null
  const permisosActuales = usuarioActual?.permisos ?? sesion?.permisos ?? []
  const rolActual = usuarioActual?.rolAcceso ?? sesion?.rolAcceso
  const esPersonal =
    usuarioActual?.estado === 'VALIDADO' && permisosActuales.length > 0
  const misPrestamos = usuarioActual
    ? datos.prestamos.filter((prestamo) => prestamo.prestatarioId === usuarioActual.id)
    : []

  useEffect(() => {
    if (pagina === 'prestamos' && !permisosActuales.includes('REGISTRAR_PRESTAMO')) {
      setPagina('catalogo')
    }
  }, [pagina, permisosActuales])

  const crearLibroCatalogo = useCallback(
    async (libro: NuevoLibroCatalogo) => {
      await api.crearLibroCatalogo(libro)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const crearEquipoCatalogo = useCallback(
    async (equipo: NuevoEquipoCatalogo) => {
      await api.crearEquipoCatalogo(equipo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const crearOtroCatalogo = useCallback(
    async (otro: NuevoOtroCatalogo) => {
      await api.crearOtroCatalogo(otro)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const actualizarTituloCatalogo = useCallback(
    async (id: string, titulo: string) => {
      await api.actualizarTituloCatalogo(id, titulo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const activarCatalogo = useCallback(
    async (id: string) => {
      await api.activarCatalogo(id)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const desactivarCatalogo = useCallback(
    async (id: string) => {
      await api.desactivarCatalogo(id)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  const crearUsuarioCuenta = useCallback(
    async (usuario: NuevoUsuarioCuenta) => {
      await api.crearUsuario(usuario)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const validarUsuarioCuenta = useCallback(
    async (id: string) => {
      await api.validarUsuario(id, OPERADOR_DEMO_ID)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const suspenderUsuarioCuenta = useCallback(
    async (id: string, motivo: string) => {
      await api.suspenderUsuario(id, OPERADOR_DEMO_ID, motivo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const reactivarUsuarioCuenta = useCallback(
    async (id: string) => {
      await api.reactivarUsuario(id, OPERADOR_DEMO_ID)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const otorgarPermisoUsuarioCuenta = useCallback(
    async (id: string, permiso: PermisoOperativo) => {
      await api.otorgarPermisoUsuario(id, OPERADOR_DEMO_ID, permiso)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const revocarPermisoUsuarioCuenta = useCallback(
    async (id: string, permiso: PermisoOperativo) => {
      await api.revocarPermisoUsuario(id, permiso, OPERADOR_DEMO_ID)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  function cerrarSesion() {
    setSesion(null)
    setPagina('resumen')
  }

  if (!sesion) {
    return (
      <Login
        alIngresar={(nuevaSesion) => {
          const usuario = datos.usuarios.find((item) => item.email === nuevaSesion.email)
          const esPersonal =
            usuario?.estado === 'VALIDADO' && (usuario?.permisos.length ?? 0) > 0
          setSesion({
            ...nuevaSesion,
            id: usuario?.id,
            permisos: usuario?.permisos ?? [],
            rolAcceso: usuario?.rolAcceso,
          })
          setPagina(nuevaSesion.publica ? 'catalogo' : esPersonal ? 'resumen' : 'catalogo')
        }}
        alVerCatalogo={() => {
          setSesion({ nombre: 'Visitante', email: '', publica: true })
          setPagina('catalogo')
        }}
      />
    )
  }

  const soloCatalogo = sesion.publica
  const paginaActual = soloCatalogo ? 'catalogo' : pagina

  function renderPagina() {
    switch (paginaActual) {
      case 'resumen':
        return <Resumen datos={datos} irA={setPagina} />
      case 'personas':
        return (
          <Personas
            usuarios={datos.usuarios}
            permisos={permisosActuales}
            crearUsuario={crearUsuarioCuenta}
            validarUsuario={validarUsuarioCuenta}
            suspenderUsuario={suspenderUsuarioCuenta}
            reactivarUsuario={reactivarUsuarioCuenta}
            otorgarPermiso={otorgarPermisoUsuarioCuenta}
            revocarPermiso={revocarPermisoUsuarioCuenta}
          />
        )
      case 'mis-prestamos':
        return (
          <MisPrestamos
            prestamos={misPrestamos}
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
          />
        )
      case 'catalogo':
        return (
          <Catalogo
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            puedeSolicitar={!soloCatalogo}
            irA={setPagina}
            permisos={permisosActuales}
            crearLibro={crearLibroCatalogo}
            crearEquipo={crearEquipoCatalogo}
            crearOtro={crearOtroCatalogo}
            actualizarTitulo={actualizarTituloCatalogo}
            activar={activarCatalogo}
            desactivar={desactivarCatalogo}
          />
        )
      case 'prestamos':
        return (
          <Prestamos
            prestamos={datos.prestamos}
            usuarios={datos.usuarios}
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            operadorId={permisosActuales.includes('REGISTRAR_PRESTAMO') ? usuarioActual?.id ?? null : null}
            puedeOperar={permisosActuales.includes('REGISTRAR_PRESTAMO')}
            crearPrestamo={crearPrestamo}
            devolverPrestamo={devolverPrestamo}
          />
        )
      case 'sanciones':
        return <Sanciones sanciones={datos.sanciones} usuarios={datos.usuarios} />
      case 'administracion':
        return <Administracion datos={datos} />
      default:
        return <Resumen datos={datos} irA={setPagina} />
    }
  }

  return (
    <div className="aplicacion">
      <BarraLateral
        pagina={paginaActual}
        cambiarPagina={setPagina}
        cerrarSesion={cerrarSesion}
        sesion={sesion}
        soloCatalogo={soloCatalogo}
        permisos={permisosActuales}
        rolAcceso={rolActual}
        esPersonal={esPersonal}
      />
      <main className="contenido">
        <Encabezado pagina={paginaActual} publica={soloCatalogo} />
        {!soloCatalogo && (
          <div className="banner-demo">
            Modo demo: la sesión es local y la autenticación real está pendiente. Los datos se
            consultan a la API en tiempo real.
          </div>
        )}
        {errorCarga && (
          <div className="aviso error-aviso">
            ⚠ {errorCarga}
            <button className="enlace" onClick={() => void cargarDatos()} type="button">
              Reintentar
            </button>
          </div>
        )}
        {cargando ? <div className="cargando">Cargando datos de la API…</div> : renderPagina()}
      </main>
    </div>
  )
}
