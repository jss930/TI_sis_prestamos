import { useCallback, useEffect, useState } from 'react'
import BarraLateral from './components/BarraLateral'
import Encabezado from './components/Encabezado'
import Login from './pages/Login'
import Resumen from './pages/Resumen'
import Personas from './pages/Personas'
import Catalogo from './pages/Catalogo'
import Prestamos from './pages/Prestamos'
import Sanciones from './pages/Sanciones'
import Administracion from './pages/Administracion'
import { OPERADOR_DEMO_ID } from './constants'
import { api, mensajeDeError } from './services/api'
import type { DatosBiblioteca, EstadoFisico, NuevoPrestamo, Pagina, Sesion } from './types'

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

  function cerrarSesion() {
    setSesion(null)
    setPagina('resumen')
  }

  if (!sesion) {
    return (
      <Login
        alIngresar={(nuevaSesion) => {
          setSesion(nuevaSesion)
          setPagina(nuevaSesion.publica ? 'catalogo' : 'resumen')
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
        return <Personas usuarios={datos.usuarios} />
      case 'catalogo':
        return (
          <Catalogo
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            puedeSolicitar={!soloCatalogo}
            irA={setPagina}
          />
        )
      case 'prestamos':
        return (
          <Prestamos
            prestamos={datos.prestamos}
            usuarios={datos.usuarios}
            catalogo={datos.catalogo}
            ejemplares={datos.ejemplares}
            operadorDemoId={OPERADOR_DEMO_ID}
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
