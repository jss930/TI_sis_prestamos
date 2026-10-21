import Tabla from '../components/Tabla'
import EtiquetaEstado from '../components/EtiquetaEstado'
export default function Sanciones({sanciones}){const columnas=[{clave:'persona',titulo:'PERSONA'},{clave:'motivo',titulo:'MOTIVO'},{clave:'desde',titulo:'DESDE'},{clave:'hasta',titulo:'HASTA'},{clave:'estado',titulo:'ESTADO',render:f=><EtiquetaEstado estado={f.estado}/>}];return <><div className="aviso">ℹ Las sanciones son calculadas por las reglas del sistema. Aquí puedes consultarlas.</div><section className="panel"><Tabla filas={sanciones} columnas={columnas}/></section></>}
