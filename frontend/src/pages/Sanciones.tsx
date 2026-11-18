import { useState } from 'react'
import { flushSync } from 'react-dom'
import Tabla, { type Columna } from '../components/Tabla'
import CabeceraReporte from '../components/CabeceraReporte'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { Sancion, Usuario } from '../types'
import { dinero, etiquetaMotivo, fechaCorta } from '../utils/format'

interface Props {
  sanciones: Sancion[]
  usuarios: Usuario[]
  usuarioId?: string
}

export default function Sanciones({ sanciones: todas, usuarios, usuarioId }: Props) {
  const [filtros, setFiltros] = useState({ desde: '', hasta: '', estado: '' })
  const [limite, setLimite] = useState(10)
  const [generadoEn, setGeneradoEn] = useState(() => new Date().toISOString())
  const sanciones = todas.filter((s) => (!usuarioId || s.usuarioId === usuarioId)
    && (!filtros.desde || s.fechaInicio >= filtros.desde) && (!filtros.hasta || s.fechaInicio <= filtros.hasta)
    && (!filtros.estado || s.estado === filtros.estado)).sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio) || b.id.localeCompare(a.id))
  const activas = sanciones.filter((sancion) => sancion.estado === 'ACTIVA')

  const columnas: Columna<Sancion>[] = [
    {
      clave: 'persona',
      titulo: 'PERSONA',
      render: (sancion) => {
        const usuario = usuarios.find((actual) => actual.id === sancion.usuarioId)
        return (
          <div className="celda-principal">
            <strong>{usuario?.nombreCompleto ?? sancion.usuarioId}</strong>
            <small>{usuario?.email ?? 'Usuario no cargado'}</small>
          </div>
        )
      },
    },
    {
      clave: 'motivo',
      titulo: 'MOTIVO',
      render: (sancion) => etiquetaMotivo(sancion.motivo),
    },
    {
      clave: 'diasSuspension',
      titulo: 'SUSPENSIÓN',
      render: (sancion) => `${sancion.diasSuspension} día(s)`,
    },
    {
      clave: 'multa',
      titulo: 'MULTA',
      render: (sancion) => dinero(sancion.multa, sancion.moneda),
    },
    {
      clave: 'periodo',
      titulo: 'PERIODO',
      render: (sancion) => `${fechaCorta(sancion.fechaInicio)} → ${fechaCorta(sancion.fechaFin)}`,
    },
    {
      clave: 'estado',
      titulo: 'ESTADO',
      render: (sancion) => <EtiquetaEstado estado={sancion.estado} />,
    },
    {
      clave: 'observaciones',
      titulo: 'OBSERVACIONES',
      render: (sancion) => (
        <span className="texto-suave">{sancion.observaciones ?? '—'}</span>
      ),
    },
  ]

  return (
    <>
      <div className="aviso no-imprimir">
        ℹ Las sanciones son calculadas por las reglas del backend. Aquí puedes consultar su
        estado y vigencia.
      </div>
      <section className="panel formulario no-imprimir">
        <h2>Consultar sanciones</h2>
        <div className="campos dos">
          <label>Desde<input type="date" value={filtros.desde} max={filtros.hasta || undefined} onChange={(e) => { setFiltros({ ...filtros, desde: e.target.value }); setLimite(10) }} /></label>
          <label>Hasta (incluido)<input type="date" value={filtros.hasta} min={filtros.desde || undefined} onChange={(e) => { setFiltros({ ...filtros, hasta: e.target.value }); setLimite(10) }} /></label>
          <label>Estado<select value={filtros.estado} onChange={(e) => { setFiltros({ ...filtros, estado: e.target.value }); setLimite(10) }}>
            <option value="">Todos</option><option value="ACTIVA">Activa</option><option value="CUMPLIDA">Cumplida</option><option value="ANULADA">Anulada</option>
          </select></label>
        </div>
        <div className="acciones-fila">
          <button className="boton secundario" type="button" onClick={() => { setFiltros({ desde: '', hasta: '', estado: '' }); setLimite(10) }}>Limpiar filtros</button>
          <button className="boton secundario" type="button" onClick={() => { flushSync(() => setGeneradoEn(new Date().toISOString())); window.print() }}>Imprimir / guardar PDF</button>
        </div>
      </section>
      <article className="reporte-imprimible">
        <CabeceraReporte titulo={usuarioId ? `Reporte de sanciones · ${usuarios.find((u) => u.id === usuarioId)?.nombreCompleto ?? 'Persona seleccionada'}` : 'Reporte de sanciones'}
          desde={filtros.desde || null} hasta={filtros.hasta || null} generadoEn={generadoEn}>
          <p>Estado: {filtros.estado || 'Todos'} · {sanciones.length} registros</p>
        </CabeceraReporte>
      <section className="panel">
        <div className="panel-titulo">
          <div>
            <h2>Sanciones registradas</h2>
            <p>
              {sanciones.length} en total · {activas.length} activa(s)
            </p>
          </div>
        </div>
        <div className="no-imprimir"><Tabla filas={sanciones.slice(0, limite)} columnas={columnas} vacio="No hay sanciones registradas" />
          <div className="acciones-fila"><p>Mostrando {Math.min(limite, sanciones.length)} de {sanciones.length} sanciones</p>
            {limite < sanciones.length && <button className="boton secundario" type="button" onClick={() => setLimite((n) => n + 10)}>Ver 10 más</button>}
          </div>
        </div>
        <div className="solo-imprimir"><Tabla filas={sanciones} columnas={columnas} vacio="No hay sanciones registradas" /></div>
      </section>
      </article>
    </>
  )
}
