import { Encabezado } from '@comun/componentes/Interfaz.jsx'
import ListaProductos from '../componentes/ListaProductos.jsx'

/** HU-16: consulta del catálogo desde el celular (precio vigente y stock disponible). */
export default function Catalogo() {
  return (
    <div className="pantalla">
      <Encabezado titulo="Catálogo" subtitulo="Precios mayoristas vigentes y stock disponible" />
      <main className="contenido"><ListaProductos /></main>
    </div>
  )
}
