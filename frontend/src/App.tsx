import { useCallback, useEffect, useState } from 'react'
import BarraLateral from './components/BarraLateral'
import Encabezado from './components/Encabezado'
import Login from './pages/Login'
import Resumen from './pages/Resumen'
import Personas from './pages/Personas'
import MiCuenta from './pages/MiCuenta'
import Catalogo from './pages/Catalogo'
import MisPrestamos from './pages/MisPrestamos'
import MisReservas from './pages/MisReservas'
import Reservas from './pages/Reservas'
import Prestamos from './pages/Prestamos'
import Sanciones from './pages/Sanciones'
import Administracion from './pages/Administracion'
import ReservaNueva from './pages/ReservaNueva'
import CatalogoGestion from './pages/CatalogoGestion'
import CatalogoAlta from './pages/CatalogoAlta'
import EjemplarAlta from './pages/EjemplarAlta'
import PrestamoNuevo from './pages/PrestamoNuevo'
import CatalogoEditar from './pages/CatalogoEditar'
import { useLocation, useNavigate } from 'react-router-dom'
import { api, mensajeDeError } from './services/api'
import type {
  DatosBiblioteca,
  DatosCuentaPendiente,
  LugarUso,
  NuevaInspeccionDevolucion,
  NuevoEquipoCatalogo,
  NuevoLibroCatalogo,
  NuevoOtroCatalogo,
  NuevoPrestamo,
  NuevoUsuarioCuenta,
  Pagina,
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
  const navegar = useNavigate()
  const location = useLocation()
  const [sesion, setSesion] = useState<Sesion | null>(() => {
    try {
      const raw = localStorage.getItem('biblioteca_sesion')
      return raw ? (JSON.parse(raw) as Sesion) : null
    } catch {
      return null
    }
  })
  const rutas: Record<Pagina, string> = {
    resumen: '/', 'mi-cuenta': '/mi-cuenta', personas: '/personas', catalogo: '/catalogo',
    'mis-prestamos': '/mis-prestamos', 'mis-reservas': '/mis-reservas', prestamos: '/prestamos',
    sanciones: '/sanciones', administracion: '/administracion', reservas: '/reservas',
    'catalogo-gestion': '/admin/catalogo', 'catalogo-nuevo': '/admin/catalogo/nuevo',
    'catalogo-editar': location.pathname,
    'ejemplar-nuevo': location.pathname, 'reserva-nueva': location.pathname, 'prestamo-nuevo': '/prestamos/nuevo',
  }
  const pagina: Pagina = location.pathname === '/' ? 'resumen'
    : location.pathname === '/admin/catalogo' ? 'catalogo-gestion'
    : location.pathname === '/admin/catalogo/nuevo' ? 'catalogo-nuevo'
    : /^\/admin\/catalogo\/[^/]+\/editar$/.test(location.pathname) ? 'catalogo-editar'
    : /^\/admin\/catalogo\/[^/]+\/ejemplares\/nuevo$/.test(location.pathname) ? 'ejemplar-nuevo'
    : /^\/catalogo\/[^/]+\/reservar$/.test(location.pathname) ? 'reserva-nueva'
    : location.pathname === '/prestamos/nuevo' ? 'prestamo-nuevo'
    : (Object.entries(rutas).find(([, ruta]) => ruta === location.pathname)?.[0] as Pagina | undefined) ?? 'catalogo'
  const irA = (destino: Pagina) => navegar(rutas[destino] ?? '/catalogo')
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
    ? datos.usuarios.find((usuario) => usuario.id === sesion.id || usuario.email === sesion.email) ?? null
    : null
  const permisosActuales = usuarioActual?.permisos ?? sesion?.permisos ?? []
  const rolActual = usuarioActual?.rolAcceso ?? sesion?.rolAcceso
  const esPersonal =
    usuarioActual?.estado === 'VALIDADO' && permisosActuales.length > 0
  const misPrestamos = usuarioActual
    ? datos.prestamos.filter((prestamo) => prestamo.prestatarioId === usuarioActual.id)
    : []

  const actualizarDatosCuenta = useCallback(async (id: string, datosCuenta: DatosCuentaPendiente) => {
    const actualizado = await api.actualizarDatosCuenta(id, datosCuenta)
    if (usuarioActual?.id === id) {
      setSesion((anterior) => {
        if (!anterior) return anterior
        const nueva = { ...anterior, nombre: actualizado.nombreCompleto, email: actualizado.email }
        localStorage.setItem('biblioteca_sesion', JSON.stringify(nueva))
        return nueva
      })
    }
    await cargarDatos(true)
  }, [cargarDatos, usuarioActual?.id])

  const solicitarCorreccionCuenta = useCallback(async (id: string, observaciones: string) => {
    await api.solicitarCorreccionCuenta(id, observaciones)
    await cargarDatos(true)
  }, [cargarDatos])

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
    async (prestatarioId: string, catalogoItemId: string, lugarUso: LugarUso, inicioReserva: string, finReserva: string) => {
      await api.solicitarReserva(prestatarioId, catalogoItemId, lugarUso, inicioReserva, finReserva)
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
    if (usuarioActual && usuarioActual.estado !== 'VALIDADO' && pagina !== 'catalogo' && pagina !== 'mi-cuenta') {
      navegar('/catalogo', { replace: true })
      return
    }
    if (pagina === 'prestamos' && !permisosActuales.includes('REGISTRAR_PRESTAMO') && !permisosActuales.includes('REGISTRAR_DEVOLUCION')) {
      navegar('/catalogo', { replace: true })
    }
    if (pagina === 'reservas' && !permisosActuales.includes('ADMINISTRAR_SISTEMA')) {
      navegar('/catalogo', { replace: true })
    }
    if (pagina === 'prestamo-nuevo' && !permisosActuales.includes('REGISTRAR_PRESTAMO')) {
      navegar('/catalogo', { replace: true })
    }
    if (['catalogo-gestion', 'catalogo-nuevo', 'catalogo-editar', 'ejemplar-nuevo'].includes(pagina)
        && !permisosActuales.includes('GESTIONAR_CATALOGO')) {
      navegar('/catalogo', { replace: true })
    }
    if (pagina === 'reserva-nueva' && usuarioActual?.estado !== 'VALIDADO') {
      navegar('/catalogo', { replace: true })
    }
  }, [pagina, permisosActuales, usuarioActual, navegar])

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
  const actualizarTituloCatalogo = useCallback(async (id: string, titulo: string) => {
    await api.actualizarTituloCatalogo(id, titulo); await cargarDatos(true)
  }, [cargarDatos])
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
  function cerrarSesion() {
    setSesion(null)
    localStorage.removeItem('biblioteca_sesion')
    localStorage.removeItem('biblioteca_token')
    setDatos((actual) => ({ ...actual, reservas: [] }))
    navegar('/')
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
          if (nuevaSesion.token) localStorage.setItem('biblioteca_token', nuevaSesion.token)
          void cargarDatos(true)
          navegar(nuevaSesion.publica ? '/catalogo' : esPersonal ? '/' : '/catalogo')
        }}
      />
    )
  }

  const soloCatalogo = sesion.publica
  const paginaActual = soloCatalogo ? 'catalogo' : pagina

  function renderPagina() {
    switch (paginaActual) {
      case 'resumen':
        return <Resumen datos={datos} irA={irA} />
      case 'mi-cuenta':
        return <MiCuenta usuario={usuarioActual} actualizar={actualizarDatosCuenta} />
      case 'personas':
        return (
          <Personas
            usuarios={datos.usuarios}
            permisos={permisosActuales}
            operadorId={usuarioActual?.id ?? null}
            crearUsuario={crearUsuarioCuenta}
            validarUsuario={validarUsuarioCuenta}
            solicitarCorreccion={solicitarCorreccionCuenta}
            actualizarDatos={actualizarDatosCuenta}
            suspenderUsuario={suspenderUsuarioCuenta}
            reactivarUsuario={reactivarUsuarioCuenta}
            otorgarPermiso={async (id, permiso) => { await api.otorgarPermisoUsuario(id, usuarioActual?.id ?? '', permiso); await cargarDatos(true) }}
            revocarPermiso={async (id, permiso) => { await api.revocarPermisoUsuario(id, permiso, usuarioActual?.id ?? ''); await cargarDatos(true) }}
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
            puedeReservar={!soloCatalogo && usuarioActual?.estado === 'VALIDADO'}
            puedeGestionar={permisosActuales.includes('GESTIONAR_CATALOGO')}
            itemsReservados={datos.reservas
              .filter((r) => r.prestatarioId === usuarioActual?.id && (r.estado === 'SOLICITADA' || r.estado === 'APROBADA'))
              .map((r) => r.catalogoItemId)}
          />
        )
      case 'reserva-nueva':
        return <ReservaNueva catalogo={datos.catalogo} reservar={(id, lugar, inicio, fin) => usuarioActual?.id ? solicitarReserva(usuarioActual.id, id, lugar, inicio, fin) : Promise.reject(new Error('Inicia sesión para reservar.'))} />
      case 'catalogo-gestion':
        return <CatalogoGestion catalogo={datos.catalogo} ejemplares={datos.ejemplares} activar={activarCatalogo} desactivar={desactivarCatalogo} />
      case 'catalogo-nuevo':
        return <CatalogoAlta crearLibro={crearLibroCatalogo} crearEquipo={crearEquipoCatalogo} crearOtro={crearOtroCatalogo} />
      case 'catalogo-editar':
        return <CatalogoEditar catalogo={datos.catalogo} actualizar={actualizarTituloCatalogo} />
      case 'ejemplar-nuevo':
        return <EjemplarAlta catalogo={datos.catalogo} crear={crearEjemplar} />
      case 'prestamo-nuevo':
        return <PrestamoNuevo usuarios={datos.usuarios} catalogo={datos.catalogo} ejemplares={datos.ejemplares} operadorId={usuarioActual?.id ?? ''} crear={crearPrestamo} />
      case 'prestamos':
        return (
          <Prestamos
            prestamos={datos.prestamos}
            usuarios={datos.usuarios}
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            puedeOperar={permisosActuales.includes('REGISTRAR_PRESTAMO')}
            puedeDevolver={permisosActuales.includes('REGISTRAR_DEVOLUCION')}
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
            reprogramarReserva={async (id, inicio, fin) => { await api.reprogramarReserva(id, inicio, fin); await cargarDatos(true) }}
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
            operadorId={permisosActuales.includes('ADMINISTRAR_SISTEMA') ? usuarioActual?.id ?? null : null}
            entregarSolicitud={async (id) => { await api.entregarSolicitud(id); await cargarDatos(true) }}
            aceptarReserva={async (id) => aceptarReserva(id, '')}
            denegarReserva={denegarReserva}
          />
        )
      case 'administracion':
        return <Administracion datos={datos} permisos={permisosActuales} />
      default:
        return <Resumen datos={datos} irA={irA} />
    }
  }

  return (
    <div className="aplicacion">
      <BarraLateral
        pagina={['catalogo-gestion','catalogo-nuevo','catalogo-editar','ejemplar-nuevo','reserva-nueva'].includes(paginaActual) ? 'catalogo' : paginaActual === 'prestamo-nuevo' ? 'prestamos' : paginaActual}
        cambiarPagina={irA}
        cerrarSesion={cerrarSesion}
        sesion={sesion}
        soloCatalogo={soloCatalogo}
        permisos={permisosActuales}
        rolAcceso={rolActual}
        esPersonal={esPersonal}
        cuentaValidada={usuarioActual?.estado === 'VALIDADO'}
      />
      <main className="contenido">
        {!['reserva-nueva','catalogo-gestion','catalogo-nuevo','catalogo-editar','ejemplar-nuevo','prestamo-nuevo'].includes(paginaActual) && <Encabezado pagina={paginaActual} publica={soloCatalogo} />}
        {usuarioActual?.estado === 'PROVISIONAL' && <div className="aviso" role="status">
          {usuarioActual.correccionPendiente ? 'Tu cuenta necesita correcciones antes de validarse.' : 'Tu cuenta está pendiente de validación. Puedes consultar el catálogo.'}
           <button className="enlace" type="button" onClick={() => navegar('/mi-cuenta')}>Revisar mi cuenta</button>
        </div>}
        {errorCarga && (
          <div className="aviso error-aviso">
            ⚠ {errorCarga}
            <button className="enlace" onClick={() => void cargarDatos()} type="button">
              Reintentar
            </button>
          </div>
        )}
        {cargando ? <div className="cargando">Preparando tu biblioteca…</div> : renderPagina()}
      </main>
    </div>
  )
}
