export default function Tabla({ columnas, filas, vacio = 'No hay registros para mostrar' }) {
  return <div className="tabla-contenedor"><table><thead><tr>{columnas.map(c => <th key={c.clave}>{c.titulo}</th>)}</tr></thead>
    <tbody>{filas.length ? filas.map((fila, indice) => <tr key={fila.id ?? indice}>{columnas.map(c =>
      <td key={c.clave}>{c.render ? c.render(fila) : fila[c.clave]}</td>)}</tr>) : <tr><td colSpan={columnas.length} className="vacio">{vacio}</td></tr>}</tbody>
  </table></div>
}
