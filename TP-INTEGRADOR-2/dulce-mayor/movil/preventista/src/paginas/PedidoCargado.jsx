import { useEffect, useState } from 'react'
import { useLocation, useParams } from 'react-router'
import { mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, BotonEnlace, Cargando, Encabezado, InsigniaPedido } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import MapaRuta from '@comun/componentes/MapaRuta.jsx'
import useUbicacion from '@comun/hooks/useUbicacion.js'
import { formatoKm, formatoMoneda } from '@comun/utilidades/formato.js'
import { pedidos, rutas } from '../servicios.js'

/** HU-20: el pedido quedó cargado y el punto visitado; se sugiere el no visitado más cercano. */
export default function PedidoCargado() {
  const { id } = useParams()
  const { state } = useLocation()
  const [pedido, setPedido] = useState(state?.pedido ?? null)
  const [delDia, setDelDia] = useState(undefined)
  const [error, setError] = useState(null)
  const { posicion, error: errorUbicacion } = useUbicacion({ seguir: false })

  useEffect(() => {
    if (!pedido) pedidos.uno(id).then(setPedido).catch((e) => setError(mensajeDeError(e)))
  }, [id, pedido])

  // Siguiente sugerido: espera la posición (o el aviso de que no hay GPS) para medir la distancia.
  const listo = posicion != null || errorUbicacion != null
  useEffect(() => {
    if (!listo) return
    rutas.miRuta(posicion).then(setDelDia).catch(() => setDelDia(null)) // sin ruta (404) o sin conexión: no hay sugerencia
  }, [listo, posicion])

  if (error) return <div className="pantalla"><Encabezado titulo="Pedido" volver="/" /><main className="contenido"><Alerta>{error}</Alerta></main></div>
  if (!pedido) return <div className="pantalla"><Encabezado titulo="Pedido" /><Cargando /></div>

  const siguiente = delDia?.siguiente
  return (
    <div className="pantalla">
      <Encabezado titulo="Pedido cargado" />
      <main className="contenido">
        <section className="tarjeta exito-cabecera">
          <span className="exito-icono"><Icono nombre="ok" tamanio={36} /></span>
          <span className="texto-secundario texto-chico">PEDIDO</span>
          <span className="exito-numero">N.º {pedido.numeroPedido}</span>
          <span className="fila-total">{formatoMoneda(pedido.total)}</span>
          <InsigniaPedido estado={pedido.estado} />
          <p className="texto-secundario texto-chico">
            {pedido.nombreFantasia}: la mercadería quedó reservada. El cliente lo confirma o lo anula desde «Mis pedidos» en el sitio de Dulce Mayor.
          </p>
        </section>

        {delDia === undefined && <Cargando texto="Buscando el siguiente punto…" />}
        {siguiente && (
          <section className="tarjeta">
            <div className="tarjeta-titulo">
              <h2>Siguiente punto sugerido</h2>
              {delDia.distanciaSiguienteKm != null && <span className="insignia insignia-magenta">{formatoKm(delDia.distanciaSiguienteKm)}</span>}
            </div>
            <p><b>{siguiente.orden}. {siguiente.nombreFantasia}</b></p>
            <p className="texto-secundario texto-chico" style={{ marginBottom: 10 }}>{siguiente.direccion}</p>
            <MapaRuta alto={180} puntos={[siguiente]} posicion={posicion} destacado={siguiente.idPuntoVenta}
              trayecto={delDia.trayecto?.geometria?.length > 1 ? { ...delDia.trayecto, geometria: tramoHasta(delDia.trayecto.geometria, siguiente) } : null} />
          </section>
        )}
        {delDia && !siguiente && <Alerta tipo="exito">No quedan puntos pendientes en tu ruta de hoy.</Alerta>}

        {siguiente
          ? <BotonEnlace a={`/puntos/${siguiente.idPuntoVenta}`}>Ir al siguiente punto<Icono nombre="flecha" /></BotonEnlace>
          : <BotonEnlace a="/">Volver a la ruta</BotonEnlace>}
        {siguiente && <BotonEnlace a="/" variante="fantasma">Elegir otro punto</BotonEnlace>}
      </main>
    </div>
  )
}

/** Recorta el trazado del día hasta el punto sugerido (el trazado va desde la posición por los pendientes en orden). */
function tramoHasta(geometria, punto) {
  let mejor = geometria.length - 1
  let distanciaMinima = Infinity
  geometria.forEach(([lat, lon], i) => {
    const d = (lat - punto.latitud) ** 2 + (lon - punto.longitud) ** 2
    if (d < distanciaMinima) { distanciaMinima = d; mejor = i }
  })
  return geometria.slice(0, mejor + 1)
}
