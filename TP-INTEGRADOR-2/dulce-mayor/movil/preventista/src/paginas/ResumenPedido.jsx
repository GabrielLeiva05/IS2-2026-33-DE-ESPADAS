import { useState } from 'react'
import { Navigate, useNavigate, useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Encabezado } from '@comun/componentes/Interfaz.jsx'
import { formatoMoneda, presentacionLegible } from '@comun/utilidades/formato.js'
import { Stepper } from '../componentes/ListaProductos.jsx'
import { useCarrito } from '../contexto/CarritoContext.jsx'
import { pedidos } from '../servicios.js'

/**
 * HU-21 (paso 2): revisar y cargar. El backend toma el precio vigente, valida el stock y lo reserva;
 * el pedido queda CARGADO hasta que el cliente lo confirme.
 */
export default function ResumenPedido() {
  const { idPuntoVenta } = useParams()
  const navegar = useNavigate()
  const carrito = useCarrito()
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [cargado, setCargado] = useState(false)

  // Sin productos elegidos no hay resumen (salvo justo después de cargar, mientras se navega al comprobante).
  if (!cargado && (carrito.idPuntoVenta !== idPuntoVenta || carrito.bultos === 0)) {
    return <Navigate to={`/puntos/${idPuntoVenta}/pedido`} replace />
  }

  async function cargar() {
    setEnviando(true)
    setError(null)
    try {
      const pedido = await pedidos.cargar(idPuntoVenta,
        carrito.lineas.map((l) => ({ idProducto: l.producto.id, cantidad: l.cantidad })))
      setCargado(true)
      carrito.vaciar()
      navegar(`/pedidos/${pedido.id}/cargado`, { replace: true, state: { pedido } })
    } catch (e) {
      setError(mensajeDeError(e, 'No se pudo cargar el pedido.'))
      setEnviando(false)
    }
  }

  return (
    <div className="pantalla">
      <Encabezado titulo="Resumen del pedido" volver={`/puntos/${idPuntoVenta}/pedido`} />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        <section className="tarjeta">
          <ul className="lista" style={{ gap: 14 }}>
            {carrito.lineas.map(({ producto, cantidad }) => (
              <li key={producto.id}>
                <div className="fila">
                  <span className="item-cuerpo">
                    <strong>{producto.nombre}</strong>
                    <small>{presentacionLegible(producto.presentacion, producto.unidadesPorPresentacion)} · {formatoMoneda(producto.precio)} c/u</small>
                  </span>
                  <Stepper nombre={producto.nombre} cantidad={cantidad} maximo={producto.stockDisponible}
                    alCambiar={(n) => carrito.cambiarCantidad(producto, n)} />
                </div>
                <div className="fila texto-chico" style={{ marginTop: 4 }}>
                  <span className="texto-secundario">Subtotal</span><b>{formatoMoneda(producto.precio * cantidad)}</b>
                </div>
              </li>
            ))}
          </ul>
          <hr className="separador" />
          <div className="fila"><span>{carrito.bultos} bultos</span><span className="fila-total">{formatoMoneda(carrito.total)}</span></div>
          <p className="ayuda" style={{ marginTop: 8 }}>
            El total final se calcula con el precio vigente al cargar. La mercadería queda reservada hasta que el cliente confirme o anule.
          </p>
        </section>
        <button type="button" className="boton boton-primario" disabled={enviando} onClick={cargar}>
          {enviando ? 'Cargando pedido…' : 'Cargar pedido'}
        </button>
      </main>
    </div>
  )
}
