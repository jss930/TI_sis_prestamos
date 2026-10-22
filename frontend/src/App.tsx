import { useCallback, useEffect, useState } from 'react'
import BarraLateral from './components/BarraLateral'
import Encabezado from './components/Encabezado'
import Login from './pages/Login'
import Resumen from './pages/Resumen'
import Personas from './pages/Personas'
import Catalogo from './pages/Catalogo'
import MisPrestamos from './pages/MisPrestamos'
import MisReservas from './pages/MisReservas'
import Reservas from './pages/Reservas'
import Prestamos from './pages/Prestamos'
import Sanciones from './pages/Sanciones'
import Administracion from './pages/Administracion'
import { api, mensajeDeError } from './services/api'
import type {
  DatosBiblioteca,
  NuevaInspeccionDevolucion,
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
  reservas: [],
}

export default function App() {
  const [sesion, setSesion] = useState<Sesion | null>(() => {
    try {
      const raw = localStorage.getItem('biblioteca_sesion')
      return raw ? (JSON.parse(raw) as Sesion) : null
    } catch {
      return null
    }
  })
  const [pagina, setPagina] = useState<Pagina>('resumen')
  const [datos, setDatos] = useState<DatosBiblioteca>(DATOS_VACIOS)
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  const cargarDatos = useCallback(async (silencioso = false) => {
    if (!silencioso) setCargando(true)
    setErrorCarga(null)
    try {
      const [catalogo, usuarios, ejemplares, prestamos, sanciones, reservas] = await Promise.all([
        api.getCatalogo(),
        api.getUsuarios(),
        api.getEjemplares(),
        api.getPrestamos(),
        api.getSanciones(),
        api.getReservas(),
      ])
      setDatos({ catalogo, usuarios, ejemplares, prestamos, sanciones, reservas })
    } catch (error) {
      setErrorCarga(mensajeDeError(error))
    } finally {
      if (!silencioso) setCargando(false)
    }
  }, [])

  useEffect(() => {
    void cargarDatos()
  }, [cargarDatos])

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

  const crearPrestamo = useCallback(
    async (nuevoPrestamo: NuevoPrestamo) => {
      await api.crearPrestamo(nuevoPrestamo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  const devolverPrestamo = useCallback(
    async (id: string, inspeccion: NuevaInspeccionDevolucion) => {
      const registrada = await api.inspeccionarDevolucion(id, inspeccion)
      await cargarDatos(true)
      return registrada
    },
    [cargarDatos],
  )

  const solicitarReserva = useCallback(
    async (prestatarioId: string, catalogoItemId: string) => {
      await api.solicitarReserva(prestatarioId, catalogoItemId)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const cancelarReserva = useCallback(
    async (id: string) => {
      if (!usuarioActual?.id) return
      await api.cancelarReserva(id, usuarioActual.id)
      await cargarDatos(true)
    },
    [cargarDatos, usuarioActual],
  )
  const aceptarReserva = useCallback(
    async (id: string, ejemplarId: string) => {
      if (!usuarioActual?.id) return
      await api.aceptarReserva(id, usuarioActual.id, ejemplarId)
      await cargarDatos(true)
    },
    [cargarDatos, usuarioActual],
  )
  const denegarReserva = useCallback(
    async (id: string, motivo: string) => {
      if (!usuarioActual?.id) return
      await api.denegarReserva(id, usuarioActual.id, motivo)
      await cargarDatos(true)
    },
    [cargarDatos, usuarioActual],
  )


  useEffect(() => {
    if (pagina === 'prestamos' && !permisosActuales.includes('REGISTRAR_PRESTAMO') && !permisosActuales.includes('REGISTRAR_DEVOLUCION')) {
      setPagina('catalogo')
    }
    if (pagina === 'reservas' && !permisosActuales.includes('GESTIONAR_RESERVAS') && !permisosActuales.includes('GESTIONAR_CATALOGO')) {
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
  const crearEjemplar = useCallback(
    async (ejemplar: import('./types').NuevoEjemplar) => {
      await api.crearEjemplar(ejemplar)
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  const crearUsuarioCuenta = useCallback(
    async (usuario: NuevoUsuarioCuenta) => {
      await api.registrarCuenta(usuario)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const validarUsuarioCuenta = useCallback(
    async (id: string) => {
      await api.validarUsuario(id, usuarioActual?.id ?? sesion?.id ?? '')
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const suspenderUsuarioCuenta = useCallback(
    async (id: string, motivo: string) => {
      await api.suspenderUsuario(id, usuarioActual?.id ?? sesion?.id ?? '', motivo)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const reactivarUsuarioCuenta = useCallback(
    async (id: string) => {
      await api.reactivarUsuario(id, usuarioActual?.id ?? sesion?.id ?? '')
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const otorgarPermisoUsuarioCuenta = useCallback(
    async (id: string, permiso: PermisoOperativo) => {
      await api.otorgarPermisoUsuario(id, usuarioActual?.id ?? sesion?.id ?? '', permiso)
      await cargarDatos(true)
    },
    [cargarDatos],
  )
  const revocarPermisoUsuarioCuenta = useCallback(
    async (id: string, permiso: PermisoOperativo) => {
      await api.revocarPermisoUsuario(id, permiso, usuarioActual?.id ?? sesion?.id ?? '')
      await cargarDatos(true)
    },
    [cargarDatos],
  )

  function cerrarSesion() {
    setSesion(null)
    localStorage.removeItem('biblioteca_sesion')
    localStorage.removeItem('biblioteca_token')
    setPagina('resumen')
  }

  if (!sesion) {
    return (
      <Login
        alIngresar={(nuevaSesion) => {
          const usuario = datos.usuarios.find((item) => item.email === nuevaSesion.email)
          const esPersonal =
            usuario?.estado === 'VALIDADO' && (usuario?.permisos.length ?? 0) > 0
          const sesionCompleta = {
            ...nuevaSesion,
            id: usuario?.id ?? nuevaSesion.id,
            permisos: usuario?.permisos ?? nuevaSesion.permisos ?? [],
            rolAcceso: usuario?.rolAcceso ?? nuevaSesion.rolAcceso,
          }
          setSesion(sesionCompleta)
          localStorage.setItem('biblioteca_sesion', JSON.stringify(sesionCompleta))
          void cargarDatos(true)
          if (nuevaSesion.token) localStorage.setItem('biblioteca_token', nuevaSesion.token)
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
            crearEjemplar={crearEjemplar}
            puedeReservar={!soloCatalogo && !!usuarioActual?.id}
            itemsReservados={datos.reservas
              .filter((r) => r.prestatarioId === usuarioActual?.id && r.estado === 'SOLICITADA')
              .map((r) => r.catalogoItemId)}
            reservar={(catalogoItemId) =>
              usuarioActual?.id ? solicitarReserva(usuarioActual.id, catalogoItemId) : Promise.resolve()
            }
          />
        )
      case 'prestamos':
        return (
          <Prestamos
            prestamos={datos.prestamos}
            usuarios={datos.usuarios}
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            operadorId={permisosActuales.includes('REGISTRAR_PRESTAMO') || permisosActuales.includes('REGISTRAR_DEVOLUCION') ? usuarioActual?.id ?? null : null}
            puedeOperar={permisosActuales.includes('REGISTRAR_PRESTAMO')}
            puedeDevolver={permisosActuales.includes('REGISTRAR_DEVOLUCION')}
            crearPrestamo={crearPrestamo}
            devolverPrestamo={devolverPrestamo}
          />
        )
      case 'sanciones':
        return <Sanciones sanciones={datos.sanciones} usuarios={datos.usuarios} />
      case 'mis-reservas':
        return (
          <MisReservas
            reservas={datos.reservas}
            catalogo={datos.catalogo}
            usuarioId={usuarioActual?.id ?? null}
            cancelarReserva={cancelarReserva}
          />
        )
      case 'reservas':
        return (
          <Reservas
            ejemplares={datos.ejemplares}
            prestamos={datos.prestamos}
            reservas={datos.reservas}
            usuarios={datos.usuarios}
            catalogo={datos.catalogo}
            operadorId={permisosActuales.includes('GESTIONAR_RESERVAS') || permisosActuales.includes('GESTIONAR_CATALOGO') ? usuarioActual?.id ?? null : null}
            aceptarReserva={aceptarReserva}
            denegarReserva={denegarReserva}
          />
        )
      case 'administracion':
        return <Administracion datos={datos} permisos={permisosActuales} />
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
