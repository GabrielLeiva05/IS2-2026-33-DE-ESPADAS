import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { esNoEncontrado, mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, BotonEnlace, Cargando, Encabezado, InsigniaPedido, InsigniaResultado } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import MapaRuta from '@comun/componentes/MapaRuta.jsx'
import { formatoMoneda } from '@comun/utilidades/formato.js'
import FormularioMotivo from '@comun/componentes/FormularioMotivo.jsx'
import { puntosVenta, rutas } from '../servicios.js'

const CONDICION_IVA = {
  RESPONSABLE_INSCRIPTO: 'Responsable inscripto', MONOTRIBUTISTA: 'Monotributista', EXENTO: 'Exento', CONSUMIDOR_FINAL: 'Consumidor final',
}
const MOTIVOS = ['Comercio cerrado', 'Tiene mercadería', 'Responsable ausente', 'Volver otro día']

/** Detalle del punto de venta: datos, estado de la visita, tomar pedido o pasar al siguiente sin pedido. */
export default function DetallePunto() {
  const { idPuntoVenta } = useParams()
  const navegar = useNavigate()
  const [punto, setPunto] = useState(null)
  const [parada, setParada] = useState(null)
  const [error, setError] = useState(null)
  const [hojaAbierta, setHojaAbierta] = useState(false)

  useEffect(() => {
    let vigente = true
    Promise.all([
      puntosVenta.uno(idPuntoVenta),
      rutas.miRuta(null).catch((e) => (esNoEncontrado(e) ? null : Promise.reject(e))),
    ])
      .then(([pv, delDia]) => {
        if (!vigente) return
        setPunto(pv)
        setParada(delDia?.ruta?.puntos?.find((p) => p.idPuntoVenta === idPuntoVenta) ?? null)
      })
      .catch((e) => { if (vigente) setError(mensajeDeError(e, 'No se pudo cargar el comercio.')) })
    return () => { vigente = false }
  }, [idPuntoVenta])

  async function sinPedido(motivo) {
    try {
      await rutas.sinPedido(idPuntoVenta, motivo)
      navegar('/', { replace: true })
    } catch (e) {
      throw new Error(mensajeDeError(e), { cause: e })
    }
  }

  if (error) return <div className="pantalla"><Encabezado titulo="Comercio" volver="/" /><main className="contenido"><Alerta>{error}</Alerta></main></div>
  if (!punto) return <div className="pantalla"><Encabezado titulo="Comercio" volver="/" /><Cargando /></div>

  const d = punto.direccion
  const conUbicacion = d?.latitud != null && d?.longitud != null
  const pendiente = parada?.resultado === 'PENDIENTE'
  const textoDireccion = [d?.calle && `${d.calle} ${d.numeracion ?? ''}`.trim(), d?.barrio, d?.localidad].filter(Boolean).join(', ')

  return (
    <div className="pantalla">
      <Encabezado titulo={punto.nombreFantasia} subtitulo={parada ? `Punto ${parada.orden} de tu ruta` : 'Fuera de tu ruta de hoy'} volver="/" />
      <main className="contenido">
        {conUbicacion && (
          <MapaRuta alto={180} puntos={[{ idPuntoVenta, orden: parada?.orden ?? '•', resultado: parada?.resultado ?? 'PENDIENTE',
            latitud: d.latitud, longitud: d.longitud, nombreFantasia: punto.nombreFantasia }]} />
        )}

        <section className="tarjeta">
          <div className="tarjeta-titulo"><h2>Datos del comercio</h2>{parada && <InsigniaResultado resultado={parada.resultado} />}</div>
          <p><Icono nombre="ubicacion" tamanio={16} /> {textoDireccion}</p>
          {d?.referencia && <p className="texto-secundario texto-chico">{d.referencia}</p>}
          <hr className="separador" />
          <p className="texto-chico"><b>Razón social:</b> {punto.razonSocial}</p>
          <p className="texto-chico"><b>CUIT:</b> {punto.cuit} · {CONDICION_IVA[punto.condicionIva] ?? punto.condicionIva}</p>
          <p className="texto-chico"><b>Responsable:</b> {punto.nombreCliente}{punto.correoCliente ? ` · ${punto.correoCliente}` : ''}</p>
          {parada?.motivo && <p className="texto-chico"><b>Motivo:</b> {parada.motivo}</p>}
          {conUbicacion && (
            <a className="texto-chico" target="_blank" rel="noreferrer"
              href={`https://www.openstreetmap.org/directions?to=${d.latitud},${d.longitud}`}>Cómo llegar (OpenStreetMap)</a>
          )}
        </section>

        {parada?.pedidos?.length > 0 && (
          <section className="tarjeta">
            <h2>Pedidos de hoy</h2>
            {parada.pedidos.map((p) => (
              <div key={p.id} className="fila" style={{ marginTop: 8 }}>
                <span>N.º {p.numeroPedido} · {formatoMoneda(p.total)}</span><InsigniaPedido estado={p.estado} />
              </div>
            ))}
          </section>
        )}

        <BotonEnlace a={`/puntos/${idPuntoVenta}/pedido`}><Icono nombre="carrito" />Tomar pedido</BotonEnlace>
        {pendiente && (
          <button type="button" className="boton boton-fantasma" onClick={() => setHojaAbierta(true)}>
            Siguiente punto sin pedido<Icono nombre="flecha" />
          </button>
        )}
      </main>
      <FormularioMotivo abierta={hojaAbierta} titulo="Visita sin pedido" sugerencias={MOTIVOS}
        textoBoton="Registrar y seguir" alCerrar={() => setHojaAbierta(false)} alConfirmar={sinPedido} />
    </div>
  )
}
