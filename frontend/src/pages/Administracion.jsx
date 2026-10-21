export default function Administracion() {
  return <section className="admin-grid">
    <article className="panel"><h2>Políticas de préstamo</h2><p>Los límites, plazos y reglas se configuran en el backend.</p><button className="boton secundario">Consultar políticas</button></article>
    <article className="panel"><h2>Estado del servicio</h2><div className="servicio"><span className="punto"/><div><strong>API del sistema</strong><small>Configurada en VITE_API_URL</small></div></div></article>
    <article className="panel"><h2>Actividad</h2><p>Consulta entregas, devoluciones y cambios de los encargados.</p><button className="boton secundario">Ver historial</button></article>
  </section>
}
