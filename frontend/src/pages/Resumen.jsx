import Tabla from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'

export default function Resumen({ datos, irA }) {
  const tarjetas = [['Préstamos activos','12','+3 hoy','↗'],['Bienes disponibles','48','de 63','▦'],['Reservas pendientes','5','por atender','◷'],['Sanciones activas','2','requieren revisión','!']]
  const columnas = [{clave:'persona',titulo:'PERSONA'},{clave:'item',titulo:'BIEN'},{clave:'devolucion',titulo:'DEVOLUCIÓN'},{clave:'estado',titulo:'ESTADO',render:f=><EtiquetaEstado estado={f.estado}/>}]
  return <><section className="tarjetas">{tarjetas.map(([titulo,valor,detalle,icono])=><article className="tarjeta" key={titulo}><div><span>{titulo}</span><strong>{valor}</strong><small>{detalle}</small></div><b>{icono}</b></article>)}</section><section className="panel"><div className="panel-titulo"><div><h2>Préstamos recientes</h2><p>Últimos movimientos registrados</p></div><button className="boton secundario" onClick={()=>irA('prestamos')}>Ver todos →</button></div><Tabla filas={datos.prestamos} columnas={columnas}/></section></>
}
