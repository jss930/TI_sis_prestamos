import type { ReactNode } from 'react'

export interface Columna<T> {
  clave: string
  titulo: string
  render?: (fila: T) => ReactNode
}

interface Props<T extends { id?: string }> {
  columnas: Columna<T>[]
  filas: T[]
  vacio?: string
}

export default function Tabla<T extends { id?: string }>({
  columnas,
  filas,
  vacio = 'No hay registros para mostrar',
}: Props<T>) {
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            {columnas.map((columna) => (
              <th key={columna.clave}>{columna.titulo}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filas.length ? (
            filas.map((fila, indice) => (
              <tr key={fila.id ?? indice}>
                {columnas.map((columna) => (
                  <td key={columna.clave}>
                    {columna.render ? columna.render(fila) : String((fila as Record<string, unknown>)[columna.clave] ?? '')}
                  </td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={columnas.length} className="vacio">
                {vacio}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
