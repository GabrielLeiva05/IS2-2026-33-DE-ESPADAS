import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Encabezado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import { formatoMoneda } from '@comun/utilidades/formato.js'
import ListaProductos from '../componentes/ListaProductos.jsx'
import { useCarrito } from '../contexto/CarritoContext.jsx'
import { puntosVenta } from '../servicios.js'

/** HU-21 (paso 1): elegir productos y cantidades para el punto de venta. */
export default function TomarPedido() {
  const { idPuntoVenta } = useParams()
  const carrito = useCarrito()
  const { iniciar } = carrito
  const [punto, setPunto] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => { iniciar(idPuntoVenta) }, [idPuntoVenta, iniciar])
  useEffect(() => {
    puntosVenta.uno(idPuntoVenta).then(setPunto).catch((e) => setError(mensajeDeError(e)))
  }, [idPuntoVenta])

  return (
    <div className="pantalla">
      <Encabezado titulo="Tomar pedido" subtitulo={punto?.nombreFantasia} volver={`/puntos/${idPuntoVenta}`} />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        <ListaProductos cantidadDe={carrito.cantidadDe} alCambiar={carrito.cambiarCantidad} />
        {carrito.bultos > 0 && <div className="espacio-carrito" />}
      </main>
      {carrito.bultos > 0 && (
        <Link to={`/puntos/${idPuntoVenta}/resumen`} className="barra-carrito">
          <span><Icono nombre="carrito" /> {carrito.bultos} {carrito.bultos === 1 ? 'bulto' : 'bultos'}</span>
          <b>{formatoMoneda(carrito.total)}</b>
          <span>Ver pedido <Icono nombre="flecha" tamanio={18} /></span>
        </Link>
      )}
    </div>
  )
}
