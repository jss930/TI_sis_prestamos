import { API_BASE_URL_PUBLICA } from '../services/api'
import type { DatosBiblioteca } from '../types'

export default function Administracion({ datos }: { datos: DatosBiblioteca }) {
  const resumen = [
    ['Catálogo', datos.catalogo.length],
    ['Ejemplares', datos.ejemplares.length],
    ['Personas', datos.usuarios.length],
    ['Préstamos', datos.prestamos.length],
    ['Sanciones', datos.sanciones.length],
  ]

  return (
    <section className="admin-grid">
      <article className="panel">
        <h2>API REST</h2>
        <p>El frontend consume la API configurada en VITE_API_BASE_URL.</p>
        <code className="codigo-bloque">{API_BASE_URL_PUBLICA}</code>
        <ul className="lista-endpoints">
          <li>GET /catalogo</li>
          <li>GET /usuarios</li>
          <li>GET /inventario/ejemplares</li>
          <li>GET /prestamos · POST /prestamos</li>
          <li>POST /prestamos/&#123;id&#125;/devolucion</li>
          <li>GET /sanciones</li>
        </ul>
      </article>

      <article className="panel">
        <h2>Datos cargados</h2>
        <p>Respuesta actual de los endpoints de lectura.</p>
        <dl className="resumen-datos">
          {resumen.map(([etiqueta, valor]) => (
            <div key={etiqueta}>
              <dt>{etiqueta}</dt>
              <dd>{valor}</dd>
            </div>
          ))}
        </dl>
      </article>

      <article className="panel">
        <h2>Autenticación</h2>
        <p>
          El acceso se realiza por selección de cuenta. La seguridad avanzada (JWT y roles reales)
          forma parte de la siguiente fase; el catálogo público está disponible sin sesión.
        </p>
        <div className="servicio">
          <span className="punto" />
          <div>
            <strong>Acceso por cuenta</strong>
            <small>Permisos operativos asignados a la cuenta seleccionada</small>
          </div>
        </div>
      </article>

      <article className="panel">
        <h2>CORS y desarrollo</h2>
        <p>
          El backend permite peticiones desde el servidor de Vite en el puerto 5173. Si cambias
          el puerto, actualiza la configuración de CORS y VITE_API_BASE_URL.
        </p>
        <div className="servicio">
          <span className="punto" />
          <div>
            <strong>http://localhost:5173</strong>
            <small>Origen permitido por WebConfig</small>
          </div>
        </div>
      </article>
    </section>
  )
}
