import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import FormularioMotivo from '@comun/componentes/FormularioMotivo.jsx'
import { Alerta, Cargando, Encabezado, Hoja, InsigniaPedido, InsigniaResultado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import MapaRuta from '@comun/componentes/MapaRuta.jsx'
import { formatoMoneda, presentacionLegible } from '@comun/utilidades/formato.js'
import { entregas } from '../servicios.js'

const MOTIVOS = ['Comercio cerrado', 'No estaba el responsable', 'Rechazó la mercadería', 'Sin tiempo en la jornada']

/** Parada de entrega: pedidos del comercio, monto a cobrar y registro de entrega (o del motivo por el que no se pudo). */
export default function DetalleEntrega() {
  const { idPuntoVenta } = useParams()
  const navegar = useNavigate()
  const [parada, setParada] = useState(null)
  const [pedidos, setPedidos] = useState([])
  const [error, setError] = useState(null)
  const [confirmando, setConfirmando] = useState(false)
  const [motivoAbierto, setMotivoAbierto] = useState(false)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    Promise.all([entregas.consultarRuta(null), entregas.pendientes()])
      .then(([delDia, pendientes]) => {
        const encontrada = delDia.ruta.puntos.find((p) => p.idPuntoVenta === idPuntoVenta)
        if (!encontrada) throw new Error('Ese comercio no está en tu ruta de hoy.')
        setParada(encontrada)
        setPedidos(pendientes.filter((p) => p.idPuntoVenta === idPuntoVenta))
      })
      .catch((e) => setError(e.response ? mensajeDeError(e) : e.message))
  }, [idPuntoVenta])

  async function registrarEntrega() {
    setEnviando(true)
    try {
      const entregados = await entregas.entregado(idPuntoVenta)
      navegar('/', { replace: true, state: { aviso: `Entrega registrada en ${parada.nombreFantasia} (${entregados.length} ${entregados.length === 1 ? 'pedido' : 'pedidos'}).` } })
    } catch (e) {
      setError(mensajeDeError(e))
      setConfirmando(false)
      setEnviando(false)
    }
  }

  async function noEntregado(motivo) {
    try {
      await entregas.noEntregado(idPuntoVenta, motivo)
      navegar('/', { replace: true, state: { aviso: `Quedó registrado. Los pedidos de ${parada.nombreFantasia} siguen pendientes y vuelven en la próxima ruta.` } })
    } catch (e) {
      throw new Error(mensajeDeError(e), { cause: e })
    }
  }

  if (error && !parada) return <div className="pantalla"><Encabezado titulo="Entrega" volver="/" /><main className="contenido"><Alerta>{error}</Alerta></main></div>
  if (!parada) return <div className="pantalla"><Encabezado titulo="Entrega" volver="/" /><Cargando /></div>

  const total = pedidos.reduce((s, p) => s + p.total, 0)
  const pendiente = parada.resultado === 'PENDIENTE'

  return (
    <div className="pantalla">
      <Encabezado titulo={parada.nombreFantasia} subtitulo={`Parada ${parada.orden}`} volver="/" />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        <MapaRuta alto={170} puntos={[parada]} />
        <section className="tarjeta">
          <div className="tarjeta-titulo"><h2>Comercio</h2><InsigniaResultado resultado={parada.resultado} /></div>
          <p><Icono nombre="ubicacion" tamanio={16} /> {parada.direccion}</p>
          <p className="texto-chico texto-secundario">Responsable: {parada.nombreCliente}</p>
          {parada.motivo && <p className="texto-chico">Motivo: {parada.motivo}</p>}
          <a className="texto-chico" target="_blank" rel="noreferrer"
            href={`https://www.openstreetmap.org/directions?to=${parada.latitud},${parada.longitud}`}>Cómo llegar (OpenStreetMap)</a>
        </section>

        {pedidos.map((p) => (
          <section className="tarjeta" key={p.id}>
            <div className="tarjeta-titulo"><h2>Pedido N.º {p.numeroPedido}</h2><InsigniaPedido estado={p.estado} /></div>
            {p.detalles.map((d) => (
              <div key={d.id} className="fila texto-chico" style={{ marginTop: 6 }}>
                <span>{d.cantidad} × {d.nombreProducto} <span className="texto-secundario">({presentacionLegible(d.presentacion, d.unidadesPorPresentacion)})</span></span>
                <b>{formatoMoneda(d.subtotal)}</b>
              </div>
            ))}
          </section>
        ))}
        {pedidos.length === 0 && <Alerta tipo="info">No quedan pedidos pendientes en este comercio.</Alerta>}

        {pedidos.length > 0 && (
          <section className="tarjeta fila">
            <span>Total a cobrar (contra entrega)</span><span className="fila-total">{formatoMoneda(total)}</span>
          </section>
        )}

        {pendiente && pedidos.length > 0 && (
          <>
            <button type="button" className="boton boton-exito" onClick={() => setConfirmando(true)}><Icono nombre="ok" />Entregado</button>
            <button type="button" className="boton boton-fantasma" onClick={() => setMotivoAbierto(true)}>No se pudo entregar</button>
          </>
        )}
      </main>

      <Hoja abierta={confirmando} titulo="¿Confirmás la entrega?" alCerrar={() => setConfirmando(false)}>
        <p className="texto-secundario">Se marcan como entregados {pedidos.length === 1 ? 'el pedido' : `los ${pedidos.length} pedidos`} de {parada.nombreFantasia} por {formatoMoneda(total)}.</p>
        <button type="button" className="boton boton-exito" disabled={enviando} onClick={registrarEntrega}>{enviando ? 'Registrando…' : 'Sí, entregado'}</button>
        <button type="button" className="boton boton-fantasma" onClick={() => setConfirmando(false)}>Cancelar</button>
      </Hoja>
      <FormularioMotivo abierta={motivoAbierto} titulo="No se pudo entregar" sugerencias={MOTIVOS} obligatorio
        textoBoton="Registrar y seguir" alCerrar={() => setMotivoAbierto(false)} alConfirmar={noEntregado} />
    </div>
  )
}
