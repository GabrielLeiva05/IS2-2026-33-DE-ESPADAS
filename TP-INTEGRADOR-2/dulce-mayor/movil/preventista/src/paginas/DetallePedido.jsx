import { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Cargando, Encabezado, InsigniaPedido } from '@comun/componentes/Interfaz.jsx'
import { formatoFechaHora, formatoMoneda, presentacionLegible } from '@comun/utilidades/formato.js'
import { pedidos } from '../servicios.js'

export default function DetallePedido() {
  const { id } = useParams()
  const [pedido, setPedido] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    pedidos.uno(id).then(setPedido).catch((e) => setError(mensajeDeError(e)))
  }, [id])

  return (
    <div className="pantalla">
      <Encabezado titulo={pedido ? `Pedido N.º ${pedido.numeroPedido}` : 'Pedido'} volver="/pedidos" />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        {!pedido && !error && <Cargando />}
        {pedido && (
          <>
            <section className="tarjeta">
              <div className="tarjeta-titulo"><h2>{pedido.nombreFantasia}</h2><InsigniaPedido estado={pedido.estado} /></div>
              <p className="texto-secundario texto-chico">{pedido.direccion}</p>
              <p className="texto-chico" style={{ marginTop: 8 }}>Cargado: {formatoFechaHora(pedido.fechaHoraCarga)}</p>
              {pedido.fechaConfirmacion && <p className="texto-chico">Confirmado por el cliente: {formatoFechaHora(pedido.fechaConfirmacion)}</p>}
              {pedido.fechaEntrega && <p className="texto-chico">Entregado: {formatoFechaHora(pedido.fechaEntrega)}</p>}
            </section>
            <section className="tarjeta">
              <h2>Productos</h2>
              {pedido.detalles.map((d) => (
                <div key={d.id} className="fila" style={{ marginTop: 10 }}>
                  <span className="item-cuerpo">
                    <strong>{d.nombreProducto}</strong>
                    <small>{d.cantidad} × {formatoMoneda(d.precioUnitario)} · {presentacionLegible(d.presentacion, d.unidadesPorPresentacion)}</small>
                  </span>
                  <b>{formatoMoneda(d.subtotal)}</b>
                </div>
              ))}
              <hr className="separador" />
              <div className="fila"><span>Total</span><span className="fila-total">{formatoMoneda(pedido.total)}</span></div>
            </section>
          </>
        )}
      </main>
    </div>
  )
}
