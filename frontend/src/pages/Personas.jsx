import { useState } from 'react'
import Tabla from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
export default function Personas({ personas }) {
  const [busqueda,setBusqueda]=useState('')
  const filtradas=personas.filter(p=>`${p.nombre} ${p.codigo} ${p.tipo}`.toLowerCase().includes(busqueda.toLowerCase()))
  const columnas=[{clave:'codigo',titulo:'CÓDIGO'},{clave:'nombre',titulo:'NOMBRE COMPLETO'},{clave:'tipo',titulo:'TIPO'},{clave:'estado',titulo:'CUENTA',render:f=><EtiquetaEstado estado={f.estado}/>}]
  return <section className="panel"><div className="panel-titulo"><div><h2>Directorio institucional</h2><p>{filtradas.length} personas registradas</p></div><input className="buscador" placeholder="Buscar por nombre o código…" value={busqueda} onChange={e=>setBusqueda(e.target.value)}/></div><Tabla filas={filtradas} columnas={columnas}/></section>
}
