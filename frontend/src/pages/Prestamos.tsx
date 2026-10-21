import { useEffect, useMemo, useState, type FormEvent } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import { ApiError, mensajeDeError } from '../services/api'
import type {
  CatalogoItem,
  Ejemplar,
  EstadoFisico,
  LugarUso,
  NuevoPrestamo,
  Prestamo,
  Usuario,
} from '../types'
import { etiquetaLugar, etiquetaPerfil, fechaCorta, tituloDeEjemplar } from '../utils/format'

interface Props {
  prestamos: Prestamo[]
  usuarios: Usuario[]
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
  operadorDemoId: string
  crearPrestamo: (datos: NuevoPrestamo) => Promise<void>
  devolverPrestamo: (id: string, estadoFisico?: EstadoFisico) => Promise<void>
}

type Mensaje = {
  tipo: 'ok' | 'error'
  texto: string
  codigo?: string
}

export default function Prestamos({
  prestamos,
  usuarios,
  catalogo,
  ejemplares,
  operadorDemoId,
  crearPrestamo,
  devolverPrestamo,
}: Props) {
  const operadores = useMemo(
    () =>
      usuarios.filter(
        (usuario) =>
          usuario.estado === 'VALIDADO' && usuario.permisos.includes('REGISTRAR_PRESTAMO'),
      ),
    [usuarios],
  )

  const [formulario, setFormulario] = useState<NuevoPrestamo>(() => ({
    operadorId: operadores.some((operador) => operador.id === operadorDemoId)
      ? operadorDemoId
      : operadores[0]?.id ?? '',
    prestatarioId: '',
    ejemplarId: '',
    lugarUso: 'EN_CAMPUS',
  }))
  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [mensaje, setMensaje] = useState<Mensaje | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [devolviendoId, setDevolviendoId] = useState<string | null>(null)

  useEffect(() => {
    setFormulario((actual) => {
      if (operadores.some((operador) => operador.id === actual.operadorId)) return actual
      const operadorId = operadores[0]?.id ?? ''
      return {
        ...actual,
        operadorId,
        prestatarioId: actual.prestatarioId === operadorId ? '' : actual.prestatarioId,
      }
    })
  }, [operadores])

  const prestatarios = useMemo(
    () =>
      usuarios.filter(
        (usuario) =>
          usuario.estado === 'VALIDADO' && usuario.id !== formulario.operadorId,
      ),
    [usuarios, formulario.operadorId],
  )

  const disponibles = useMemo(
    () => ejemplares.filter((ejemplar) => ejemplar.estadoFisico === 'DISPONIBLE'),
    [ejemplares],
  )

  const ordenados = useMemo(
    () => [...prestamos].sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio)),
    [prestamos],
  )

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!formulario.operadorId || !formulario.prestatarioId || !formulario.ejemplarId) {
      setMensaje({ tipo: 'error', texto: 'Selecciona operador, prestatario y ejemplar.' })
      return
    }

    setEnviando(true)
    setMensaje(null)
    try {
      await crearPrestamo(formulario)
      setMensaje({ tipo: 'ok', texto: 'Préstamo registrado correctamente.' })
      setFormulario((actual) => ({ ...actual, prestatarioId: '', ejemplarId: '' }))
      setMostrarFormulario(false)
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: mensajeDeError(error),
        codigo: error instanceof ApiError ? error.codigo : undefined,
      })
    } finally {
      setEnviando(false)
    }
  }

  async function devolver(prestamo: Prestamo) {
    setDevolviendoId(prestamo.id)
    setMensaje(null)
    try {
      await devolverPrestamo(prestamo.id)
      setMensaje({ tipo: 'ok', texto: 'Devolución registrada correctamente.' })
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: mensajeDeError(error),
        codigo: error instanceof ApiError ? error.codigo : undefined,
      })
    } finally {
      setDevolviendoId(null)
    }
  }

  const columnas: Columna<Prestamo>[] = [
    {
      clave: 'prestatario',
      titulo: 'PERSONA',
      render: (prestamo) => {
        const prestatario = usuarios.find((usuario) => usuario.id === prestamo.prestatarioId)
        return (
          <div className="celda-principal">
            <strong>{prestatario?.nombreCompleto ?? prestamo.prestatarioId}</strong>
            <small>{prestatario?.email ?? 'Usuario no cargado'}</small>
          </div>
        )
      },
    },
    {
      clave: 'bien',
      titulo: 'BIEN',
      render: (prestamo) => (
        <div className="celda-principal">
          <strong>{tituloDeEjemplar(ejemplares, catalogo, prestamo.ejemplarId)}</strong>
          <small>{etiquetaLugar(prestamo.lugarUso)}</small>
        </div>
      ),
    },
    {
      clave: 'entrega',
      titulo: 'ENTREGA',
      render: (prestamo) => fechaCorta(prestamo.fechaInicio),
    },
    {
      clave: 'vencimiento',
      titulo: 'VENCE',
      render: (prestamo) => (
        <div className="celda-principal">
          <span>{fechaCorta(prestamo.fechaVencimiento)}</span>
          {prestamo.vencido && (
            <small className="texto-peligro">{prestamo.diasRetraso} días de retraso</small>
          )}
        </div>
      ),
    },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (prestamo) => (
        <EtiquetaEstado estado={prestamo.vencido ? 'VENCIDO' : prestamo.estado} />
      ),
    },
    {
      clave: 'accion',
      titulo: 'ACCIÓN',
      render: (prestamo) =>
        prestamo.estado === 'ACTIVO' ? (
          <button
            className="enlace"
            disabled={devolviendoId === prestamo.id}
            onClick={() => void devolver(prestamo)}
            type="button"
          >
            {devolviendoId === prestamo.id ? 'Registrando…' : 'Registrar devolución'}
          </button>
        ) : (
          <span className="texto-suave">—</span>
        ),
    },
  ]

  return (
    <>
      <div className="acciones">
        <button
          className="boton primario"
          disabled={!operadores.length || !prestatarios.length || !disponibles.length}
          onClick={() => setMostrarFormulario((visible) => !visible)}
          type="button"
        >
          {mostrarFormulario ? 'Cancelar registro' : '+ Registrar préstamo'}
        </button>
      </div>

      {mensaje && (
        <div className={`aviso ${mensaje.tipo === 'ok' ? 'correcto-aviso' : 'error-aviso'}`}>
          {mensaje.tipo === 'ok' ? '✓ ' : '⚠ '}
          {mensaje.texto}
          {mensaje.codigo && <small className="codigo-error">Código: {mensaje.codigo}</small>}
        </div>
      )}

      {!operadores.length && (
        <div className="aviso error-aviso">
          No hay operadores validados con permiso REGISTRAR_PRESTAMO. El backend rechazará el
          registro hasta que exista uno.
        </div>
      )}

      {mostrarFormulario && (
        <form className="formulario panel" onSubmit={enviar}>
          <h2>Nuevo préstamo</h2>
          <p>El backend validará sanción activa, cuenta habilitada y autopréstamo.</p>
          <div className="campos">
            <label>
              Operador
              <select
                required
                value={formulario.operadorId}
                onChange={(evento) => {
                  const operadorId = evento.target.value
                  setFormulario((actual) => ({
                    ...actual,
                    operadorId,
                    prestatarioId:
                      actual.prestatarioId === operadorId ? '' : actual.prestatarioId,
                  }))
                }}
              >
                <option value="">Seleccionar…</option>
                {operadores.map((operador) => (
                  <option value={operador.id} key={operador.id}>
                    {operador.nombreCompleto} — {operador.email}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Prestatario
              <select
                required
                value={formulario.prestatarioId}
                onChange={(evento) =>
                  setFormulario({ ...formulario, prestatarioId: evento.target.value })
                }
              >
                <option value="">Seleccionar…</option>
                {prestatarios.map((usuario) => (
                  <option value={usuario.id} key={usuario.id}>
                    {usuario.nombreCompleto} — {etiquetaPerfil(usuario.perfil)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Ejemplar disponible
              <select
                required
                value={formulario.ejemplarId}
                onChange={(evento) =>
                  setFormulario({ ...formulario, ejemplarId: evento.target.value })
                }
              >
                <option value="">Seleccionar…</option>
                {disponibles.map((ejemplar) => (
                  <option value={ejemplar.id} key={ejemplar.id}>
                    {tituloDeEjemplar(ejemplares, catalogo, ejemplar.id)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Lugar de uso
              <select
                value={formulario.lugarUso}
                onChange={(evento) =>
                  setFormulario({
                    ...formulario,
                    lugarUso: evento.target.value as LugarUso,
                  })
                }
              >
                <option value="EN_CAMPUS">En campus</option>
                <option value="DOMICILIO">Domicilio</option>
              </select>
            </label>
          </div>
          <button className="boton primario" disabled={enviando} type="submit">
            {enviando ? 'Registrando…' : 'Registrar préstamo'}
          </button>
        </form>
      )}

      <section className="panel">
        <div className="panel-titulo">
          <div>
            <h2>Historial de préstamos</h2>
            <p>{ordenados.length} registros</p>
          </div>
        </div>
        <Tabla
          filas={ordenados}
          columnas={columnas}
          vacio="Todavía no hay préstamos registrados"
        />
      </section>
    </>
  )
}
