import { useState } from 'react'
import BarraLateral from './components/BarraLateral'
import Encabezado from './components/Encabezado'
import Login from './pages/Login'
import Resumen from './pages/Resumen'
import Personas from './pages/Personas'
import Catalogo from './pages/Catalogo'
import Prestamos from './pages/Prestamos'
import Reservas from './pages/Reservas'
import Sanciones from './pages/Sanciones'
import Administracion from './pages/Administracion'
import { datosDemo } from './data/datosDemo'

export default function App(){
 const [usuario,setUsuario]=useState(null); const [pagina,setPagina]=useState('resumen'); const [datos]=useState(datosDemo)
 const paginas={resumen:<Resumen datos={datos} irA={setPagina}/>,personas:<Personas personas={datos.personas}/>,catalogo:<Catalogo items={datos.items}/>,prestamos:<Prestamos prestamos={datos.prestamos} personas={datos.personas} items={datos.items} registrar={()=>{}}/>,reservas:<Reservas reservas={datos.reservas}/>,sanciones:<Sanciones sanciones={datos.sanciones}/>,administracion:<Administracion/>}
 if(!usuario)return <Login alIngresar={setUsuario}/>
 return <div className="aplicacion"><BarraLateral pagina={pagina} cambiarPagina={setPagina} cerrarSesion={()=>setUsuario(null)}/><main className="contenido"><Encabezado pagina={pagina}/>{paginas[pagina]}</main></div>
}
