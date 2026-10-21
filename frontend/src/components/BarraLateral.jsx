const opciones = [
  ['resumen', '⌂', 'Resumen'], ['personas', '♙', 'Personas'], ['catalogo', '▦', 'Catálogo'],
  ['prestamos', '↗', 'Préstamos'], ['reservas', '◷', 'Reservas'], ['sanciones', '!', 'Sanciones'],
  ['administracion', '⚙', 'Administración'],
]

export default function BarraLateral({ pagina, cambiarPagina, cerrarSesion }) {
  return <aside className="barra-lateral">
    <div className="marca"><span className="marca-icono">B</span><div><strong>Biblioteca</strong><small>UNSA</small></div></div>
    <nav>{opciones.map(([id, icono, texto]) =>
      <button key={id} className={pagina === id ? 'activo' : ''} onClick={() => cambiarPagina(id)}>
        <span>{icono}</span>{texto}
      </button>)}</nav>
    <div className="usuario-mini"><div className="avatar">ME</div><div><strong>María Elena</strong><small>Encargada</small></div></div>
    <button className="cerrar" onClick={cerrarSesion}>↪ Cerrar sesión</button>
  </aside>
}
