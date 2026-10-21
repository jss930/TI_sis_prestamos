import { useState } from 'react'
import Tabla, { type Columna } from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
import type { Usuario } from '../types'
import { etiquetaPerfil } from '../utils/format'

export default function Personas({ usuarios }: { usuarios: Usuario[] }) {
  const [busqueda, setBusqueda] = useState('')
  const filtradas = usuarios.filter((usuario) =>
    `${usuario.nombreCompleto} ${usuario.email} ${usuario.perfil} ${usuario.estado}`
      .toLowerCase()
      .includes(busqueda.toLowerCase()),
  )

  const columnas: Columna<Usuario>[] = [
    { clave: 'nombreCompleto', titulo: 'NOMBRE COMPLETO' },
    { clave: 'email', titulo: 'CORREO' },
    {
      clave: 'perfil',
      titulo: 'PERFIL',
      render: (usuario) => etiquetaPerfil(usuario.perfil),
    },
    {
      clave: 'estado',
      titulo: 'CUENTA',
      render: (usuario) => <EtiquetaEstado estado={usuario.estado} />,
    },
    {
      clave: 'permisos',
      titulo: 'PERMISOS',
      render: (usuario) =>
        usuario.permisos.length ? (
          <span className="permisos-lista">
            {usuario.permisos.map((permiso) => (
              <span className="chip" key={permiso}>
                {permiso}
              </span>
            ))}
          </span>
        ) : (
          <span className="texto-suave">—</span>
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
      <Tabla
        filas={filtradas}
        columnas={columnas}
        vacio="No hay personas que coincidan con la búsqueda"
      />
    </section>
  )
}
