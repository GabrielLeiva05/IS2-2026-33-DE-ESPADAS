import { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Cargando, Encabezado, InsigniaPedido } from '@comun/componentes/Interfaz.jsx'
import { formatoFechaHora, formatoMoneda, presentacionLegible } from '@comun/utilidades/formato.js'
import { pedidos } from '../servicios.js'

const FORMAS_DE_PAGO = [
  { valor: 'EFECTIVO', texto: 'Efectivo', detalle: 'Se paga al recibir el pedido. Se envía por correo el detalle y el total.' },
  { valor: 'TRANSFERENCIA', texto: 'Transferencia', detalle: 'Se envía por correo el detalle, el total y el alias para transferir.' },
  { valor: 'QR', texto: 'QR de Mercado Pago', detalle: 'Se envía por correo el detalle, el total y el QR. Al acreditarse llega otro correo.' },
]

const ETIQUETAS_FORMA = { EFECTIVO: 'Efectivo', TRANSFERENCIA: 'Transferencia', QR: 'QR de Mercado Pago' }
const ETIQUETAS_PAGO = { PENDIENTE: 'Pendiente', APROBADO: 'Pagado', RECHAZADO: 'Rechazado' }

export default function DetallePedido() {
  const { id } = useParams()
  const [pedido, setPedido] = useState(null)
  const [error, setError] = useState(null)
  const [formaDePago, setFormaDePago] = useState('EFECTIVO')
  const [enviando, setEnviando] = useState(false)
  const [exito, setExito] = useState(false)

  useEffect(() => {
    pedidos.uno(id).then(setPedido).catch((e) => setError(mensajeDeError(e)))
  }, [id])

  async function confirmar() {
    setEnviando(true)
    setError(null)
    try {
      const actualizado = await pedidos.confirmar(id, formaDePago)
      setPedido(actualizado)
      setExito(true)
    } catch (e) {
      setError(mensajeDeError(e))
    } finally {
      setEnviando(false)
    }
  }

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

            {pedido.formaDePago && (
              <section className="tarjeta">
                <h2>Pago</h2>
                <p className="texto-chico">Medio: <strong>{ETIQUETAS_FORMA[pedido.formaDePago] ?? pedido.formaDePago}</strong></p>
                <p className="texto-chico">Estado: <strong>{ETIQUETAS_PAGO[pedido.estadoPago] ?? 'Pendiente'}</strong></p>
              </section>
            )}

            {exito && (
              <Alerta tipo="exito">Pedido confirmado. Le enviamos al cliente el detalle y las instrucciones de pago por correo.</Alerta>
            )}

            {pedido.puedeConfirmarse && !exito && (
              <section className="tarjeta">
                <h2>Confirmar y elegir medio de pago</h2>
                <p className="texto-secundario texto-chico">
                  El pedido se confirma en forma presencial. El cliente recibe por correo el detalle, el total y las instrucciones de pago.
                </p>
                <div role="radiogroup" aria-label="Medio de pago">
                  {FORMAS_DE_PAGO.map((f) => (
                    <label key={f.valor} style={{ display: 'flex', gap: 10, alignItems: 'flex-start', padding: 10, border: '1px solid #e3e3e3', borderRadius: 8, marginBottom: 8 }}>
                      <input type="radio" name="formaDePago" value={f.valor} checked={formaDePago === f.valor}
                        onChange={() => setFormaDePago(f.valor)} />
                      <span>
                        <strong>{f.texto}</strong>
                        <small className="texto-secundario texto-chico" style={{ display: 'block' }}>{f.detalle}</small>
                      </span>
                    </label>
                  ))}
                </div>
                <button type="button" className="btn btn-primario btn-bloque mt-4" onClick={confirmar} disabled={enviando}>
                  {enviando ? 'Confirmando…' : 'Confirmar pedido y enviar correo'}
                </button>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}
