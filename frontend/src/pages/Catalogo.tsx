import { useState } from 'react'
import type {
  CatalogoItem,
  Ejemplar,
  NuevoEquipoCatalogo,
  NuevoEjemplar,
  NuevoLibroCatalogo,
  NuevoOtroCatalogo,
  Pagina,
  PermisoOperativo,
} from '../types'
import { descripcionItem } from '../utils/format'
import { imagenCatalogo } from '../utils/imagenesCatalogo'

const CATEGORIAS: Array<{ id: 'TODOS' | CatalogoItem['tipo']; texto: string }> = [
  { id: 'TODOS', texto: 'Todos' },
  { id: 'LIBRO', texto: 'Libros' },
  { id: 'EQUIPO', texto: 'Equipos' },
  { id: 'OTRO', texto: 'Otros' },
]

const ICONOS: Record<CatalogoItem['tipo'], string> = {
  LIBRO: '▤',
  EQUIPO: '▣',
  OTRO: '●',
}

interface Props {
  catalogo: CatalogoItem[]
  ejemplares: Ejemplar[]
  puedeSolicitar: boolean
  irA: (pagina: Pagina) => void
  permisos: PermisoOperativo[]
  puedeReservar?: boolean
  itemsReservados?: string[]
  reservar?: (catalogoItemId: string) => Promise<void>
  crearLibro: (libro: NuevoLibroCatalogo) => Promise<void>
  crearEquipo: (equipo: NuevoEquipoCatalogo) => Promise<void>
  crearOtro: (otro: NuevoOtroCatalogo) => Promise<void>
  actualizarTitulo: (id: string, titulo: string) => Promise<void>
  activar: (id: string) => Promise<void>
  desactivar: (id: string) => Promise<void>
  crearEjemplar?: (ejemplar: NuevoEjemplar) => Promise<void>
}

export default function Catalogo({
  catalogo,
  ejemplares,
  puedeSolicitar,
  irA,
  permisos,
  puedeReservar = false,
  itemsReservados = [],
  reservar,
  crearLibro,
  crearEquipo,
  crearOtro,
  actualizarTitulo,
  activar,
  desactivar,
  crearEjemplar,
}: Props) {
  const [categoria, setCategoria] = useState<'TODOS' | CatalogoItem['tipo']>('TODOS')
  const [busqueda, setBusqueda] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [reservando, setReservando] = useState<string | null>(null)
  const [editando, setEditando] = useState<string | null>(null)
  const [nuevoTitulo, setNuevoTitulo] = useState('')
  const [gestionandoEjemplar, setGestionandoEjemplar] = useState<string | null>(null)
  const [formEjemplar, setFormEjemplar] = useState({ barcode: '', sala: 'Sala general', estante: 'Estante A-1', notas: '' })
  const [tipoAlta, setTipoAlta] = useState<'LIBRO' | 'EQUIPO' | 'OTRO'>('LIBRO')
  const [formLibro, setFormLibro] = useState<NuevoLibroCatalogo>({
    titulo: '',
    isbn: '',
    autor: '',
    editorial: '',
    anioPublicacion: 2024,
  })
  const [formEquipo, setFormEquipo] = useState<NuevoEquipoCatalogo>({
    titulo: '',
    marca: '',
    modelo: '',
  })
  const [formOtro, setFormOtro] = useState<NuevoOtroCatalogo>({ titulo: '', descripcion: '' })

  const puedeGestionar = permisos.includes('GESTIONAR_CATALOGO')

  const disponiblesPorItem = new Map<string, number>()
  for (const ejemplar of ejemplares) {
    if (ejemplar.estadoFisico === 'DISPONIBLE') {
      disponiblesPorItem.set(
        ejemplar.catalogoItemId,
        (disponiblesPorItem.get(ejemplar.catalogoItemId) ?? 0) + 1,
      )
    }
  }

  const consulta = busqueda.trim().toLocaleLowerCase('es')
  const visibles = catalogo.filter((item) => {
    const coincideCategoria = categoria === 'TODOS' || item.tipo === categoria
    const codigos = ejemplares
      .filter((ejemplar) => ejemplar.catalogoItemId === item.id)
      .map((ejemplar) => ejemplar.barcode)
    const texto = [item.titulo, item.id, item.detalle.isbn, ...codigos]
      .filter(Boolean).join(' ').toLocaleLowerCase('es')
    return coincideCategoria && texto.includes(consulta)
  })

  async function ejecutar(accion: () => Promise<void>) {
    setError(null)
    try {
      await accion()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo completar la operacion.')
    }
  }

  return (
    <>
      <label className="busqueda-catalogo">
        Buscar en el catálogo
        <input
          className="buscador"
          type="search"
          placeholder="Nombre, ISBN o código de inventario"
          value={busqueda}
          onChange={(evento) => setBusqueda(evento.target.value)}
        />
      </label>
      <p role="status">{visibles.length} bienes encontrados</p>
      {puedeGestionar && (
        <section className="panel">
          <h2>Gestion del catalogo</h2>
          <div className="filtros">
            {(['LIBRO', 'EQUIPO', 'OTRO'] as const).map((tipo) => (
              <button
                key={tipo}
                className={tipoAlta === tipo ? 'activo' : ''}
                onClick={() => setTipoAlta(tipo)}
                type="button"
              >
                {tipo}
              </button>
            ))}
          </div>
          {tipoAlta === 'LIBRO' && (
            <form
              className="form-inline"
              onSubmit={(e) => {
                e.preventDefault()
                void ejecutar(async () => {
                  await crearLibro(formLibro)
                  setFormLibro({ titulo: '', isbn: '', autor: '', editorial: '', anioPublicacion: 2024 })
                })
              }}
            >
              <input placeholder="Titulo" value={formLibro.titulo} onChange={(e) => setFormLibro({ ...formLibro, titulo: e.target.value })} required />
              <input placeholder="ISBN" value={formLibro.isbn} onChange={(e) => setFormLibro({ ...formLibro, isbn: e.target.value })} required />
              <input placeholder="Autor" value={formLibro.autor} onChange={(e) => setFormLibro({ ...formLibro, autor: e.target.value })} required />
              <input placeholder="Editorial" value={formLibro.editorial} onChange={(e) => setFormLibro({ ...formLibro, editorial: e.target.value })} required />
              <input type="number" placeholder="Anio" value={formLibro.anioPublicacion} onChange={(e) => setFormLibro({ ...formLibro, anioPublicacion: Number(e.target.value) })} required />
              <button className="boton primario" type="submit">Crear libro</button>
            </form>
          )}
          {tipoAlta === 'EQUIPO' && (
            <form
              className="form-inline"
              onSubmit={(e) => {
                e.preventDefault()
                void ejecutar(async () => {
                  await crearEquipo(formEquipo)
                  setFormEquipo({ titulo: '', marca: '', modelo: '' })
                })
              }}
            >
              <input placeholder="Titulo" value={formEquipo.titulo} onChange={(e) => setFormEquipo({ ...formEquipo, titulo: e.target.value })} required />
              <input placeholder="Marca" value={formEquipo.marca} onChange={(e) => setFormEquipo({ ...formEquipo, marca: e.target.value })} required />
              <input placeholder="Modelo" value={formEquipo.modelo} onChange={(e) => setFormEquipo({ ...formEquipo, modelo: e.target.value })} required />
              <button className="boton primario" type="submit">Crear equipo</button>
            </form>
          )}
          {tipoAlta === 'OTRO' && (
            <form
              className="form-inline"
              onSubmit={(e) => {
                e.preventDefault()
                void ejecutar(async () => {
                  await crearOtro(formOtro)
                  setFormOtro({ titulo: '', descripcion: '' })
                })
              }}
            >
              <input placeholder="Titulo" value={formOtro.titulo} onChange={(e) => setFormOtro({ ...formOtro, titulo: e.target.value })} required />
              <input placeholder="Descripcion" value={formOtro.descripcion} onChange={(e) => setFormOtro({ ...formOtro, descripcion: e.target.value })} required />
              <button className="boton primario" type="submit">Crear recurso</button>
            </form>
          )}
        </section>
      )}

      {error && <div className="aviso error-aviso">{error}</div>}

      <div className="filtros">
        {CATEGORIAS.map((opcion) => (
          <button
            className={categoria === opcion.id ? 'activo' : ''}
            onClick={() => setCategoria(opcion.id)}
            key={opcion.id}
            type="button"
          >
            {opcion.texto}
          </button>
        ))}
      </div>
      <section className="catalogo-grid">
        {visibles.map((item) => {
          const disponibles = disponiblesPorItem.get(item.id) ?? 0
          const habilitado = item.activo && disponibles > 0
          return (
            <article className="item" key={item.id}>
              <div className={`item-imagen ${item.tipo.toLowerCase()}`}><img src={imagenCatalogo(item)} alt={`Imagen de ${item.titulo}`} loading="lazy" onError={(e) => { e.currentTarget.style.display = 'none' }} /><span aria-hidden="true">{ICONOS[item.tipo]}</span></div>
              <small>
                {item.tipo} · {item.activo ? 'Activo' : 'Inactivo'}
              </small>
              {editando === item.id ? (
                <form
                  onSubmit={(e) => {
                    e.preventDefault()
                    void ejecutar(async () => {
                      await actualizarTitulo(item.id, nuevoTitulo)
                      setEditando(null)
                      setNuevoTitulo('')
                    })
                  }}
                >
                  <input value={nuevoTitulo} onChange={(e) => setNuevoTitulo(e.target.value)} required />
                  <button className="boton secundario" type="submit">Guardar</button>
                  <button className="boton secundario" type="button" onClick={() => setEditando(null)}>
                    Cancelar
                  </button>
                </form>
              ) : (
                <h3>{item.titulo}</h3>
              )}
              <p className="item-descripcion">{descripcionItem(item)}</p>
              {item.tipo === 'LIBRO' && item.detalle.isbn && (
                <p className="item-meta">ISBN {item.detalle.isbn}</p>
              )}
              <div className="disponibilidad">
                <span className={habilitado ? 'punto' : 'punto no'} />
                {habilitado ? `${disponibles} disponibles` : 'No disponible'}
              </div>
              {puedeGestionar && gestionandoEjemplar === item.id && (
                <form
                  className="form-inline"
                  onSubmit={(e) => {
                    e.preventDefault()
                    void ejecutar(async () => {
                      if (crearEjemplar) {
                        await crearEjemplar({
                          catalogoItemId: item.id,
                          barcode: formEjemplar.barcode,
                          sala: formEjemplar.sala,
                          estante: formEjemplar.estante,
                          notas: formEjemplar.notas || null,
                        })
                        setGestionandoEjemplar(null)
                      }
                    })
                  }}
                >
                  <input
                    placeholder="Barcode"
                    value={formEjemplar.barcode}
                    onChange={(e) => setFormEjemplar((f) => ({ ...f, barcode: e.target.value }))}
                    required
                  />
                  <input
                    placeholder="Sala"
                    value={formEjemplar.sala}
                    onChange={(e) => setFormEjemplar((f) => ({ ...f, sala: e.target.value }))}
                    required
                  />
                  <input
                    placeholder="Estante"
                    value={formEjemplar.estante}
                    onChange={(e) => setFormEjemplar((f) => ({ ...f, estante: e.target.value }))}
                    required
                  />
                  <input
                    placeholder="Notas"
                    value={formEjemplar.notas}
                    onChange={(e) => setFormEjemplar((f) => ({ ...f, notas: e.target.value }))}
                  />
                  <button className="boton primario" type="submit">Guardar ejemplar</button>
                </form>
              )}
              <div className="acciones-fila">
                <button
                  className="boton secundario"
                  disabled={!habilitado || !puedeSolicitar}
                  onClick={() => irA('prestamos')}
                  type="button"
                >
                  {puedeSolicitar ? 'Solicitar préstamo' : 'Consulta en ventanilla'}
                </button>
                {puedeReservar && item.activo && (
                  <button
                    className="boton secundario"
                    disabled={reservando === item.id || itemsReservados.includes(item.id)}
                    aria-busy={reservando === item.id}
                    onClick={() =>
                      void ejecutar(async () => {
                        setReservando(item.id)
                        try {
                          if (reservar) await reservar(item.id)
                        } finally {
                          setReservando(null)
                        }
                      })
                    }
                    type="button"
                  >
                    {reservando === item.id
                      ? 'Reservando…'
                      : itemsReservados.includes(item.id)
                        ? 'Reserva solicitada'
                        : 'Reservar'}
                  </button>
                )}
                {puedeGestionar && (
                  <>
                    <button
                      className="boton secundario"
                      onClick={() => {
                        setEditando(item.id)
                        setNuevoTitulo(item.titulo)
                      }}
                      type="button"
                    >
                      Editar titulo
                    </button>
                    <button
                      className="boton secundario"
                      onClick={() =>
                        void ejecutar(() => (item.activo ? desactivar(item.id) : activar(item.id)))
                      }
                      type="button"
                    >
                      {item.activo ? 'Desactivar' : 'Activar'}
                    </button>
                    <button
                      className="boton secundario"
                      onClick={() => {
                        setGestionandoEjemplar(gestionandoEjemplar === item.id ? null : item.id)
                        setFormEjemplar({ barcode: '', sala: 'Sala general', estante: 'Estante A-1', notas: '' })
                      }}
                      type="button"
                    >
                      {gestionandoEjemplar === item.id ? 'Cerrar' : 'Añadir ejemplar'}
                    </button>
                  </>
                )}
              </div>
            </article>
          )
        })}
      </section>
      {!visibles.length && <div className="aviso">No hay bienes en esta categoría.</div>}
    </>
  )
}