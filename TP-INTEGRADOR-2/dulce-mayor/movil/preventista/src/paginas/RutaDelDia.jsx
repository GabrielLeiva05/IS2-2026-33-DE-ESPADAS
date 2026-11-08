import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { esNoEncontrado, mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, BotonEnlace, Cargando, Encabezado, InsigniaResultado, Vacio } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import MapaRuta from '@comun/componentes/MapaRuta.jsx'
import NotaRecorrido from '@comun/componentes/NotaRecorrido.jsx'
import useUbicacion from '@comun/hooks/useUbicacion.js'
import { formatoKm } from '@comun/utilidades/formato.js'
import { rutas } from '../servicios.js'

const TONO_NUMERO = { CON_PEDIDO: 'numero-hecho', SIN_PEDIDO: 'numero-aviso' }

/** HU-19 / HU-20: ruta del día en el mapa, con el orden optimizado en el backend desde la ubicación del preventista. */
export default function RutaDelDia() {
  const { posicion, error: errorUbicacion, buscando, manual, elegirManual, volverAlGps } = useUbicacion()
  const [datos, setDatos] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [sinRuta, setSinRuta] = useState(false)
  const [error, setError] = useState(null)
  const [marcandoInicio, setMarcandoInicio] = useState(false)
  const [ordenando, setOrdenando] = useState(false)
  const ordenadaDesde = useRef(null)

  // Primera carga: la ruta tal como está guardada (sin depender del GPS).
  useEffect(() => {
    let vigente = true
    rutas.miRuta(null)
      .then((d) => { if (vigente) setDatos((actual) => actual ?? d) }) // si ya se ordenó, gana lo más nuevo
      .catch((e) => {
        if (!vigente) return
        if (esNoEncontrado(e)) setSinRuta(true)
        else setError(mensajeDeError(e))
      })
      .finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [])

  const ordenar = useCallback(async (desde) => {
    setOrdenando(true)
    setError(null)
    try {
      setDatos(await rutas.ordenar(desde))
      setSinRuta(false)
    } catch (e) {
      if (esNoEncontrado(e)) setSinRuta(true)
      else setError(mensajeDeError(e, 'No se pudo ordenar la ruta.'))
    } finally {
      setOrdenando(false)
    }
  }, [])

  // Con la primera posición (GPS o marcada a mano) el backend optimiza el orden de los pendientes.
  useEffect(() => {
    if (!posicion || sinRuta) return
    // El GPS se actualiza seguido: se ordena una sola vez con él; un inicio marcado a mano reordena cada vez.
    const clave = posicion.manual ? `manual:${posicion.latitud},${posicion.longitud}` : 'gps'
    if (ordenadaDesde.current === clave) return
    ordenadaDesde.current = clave
    ordenar(posicion)
  }, [posicion, sinRuta, ordenar])

  const tocarMapa = (lat, lon) => {
    elegirManual(lat, lon)
    setMarcandoInicio(false)
  }

  const accionNuevo = (
    <Link to="/clientes/nuevo" className="encabezado-accion" aria-label="Nuevo cliente"><Icono nombre="mas" /></Link>
  )

  if (cargando) {
    return <div className="pantalla"><Encabezado titulo="Ruta del día" accion={accionNuevo} /><Cargando /></div>
  }

  if (sinRuta) {
    return (
      <div className="pantalla">
        <Encabezado titulo="Ruta del día" accion={accionNuevo} />
        <main className="contenido">
          <Vacio icono="ruta" titulo="No tenés una ruta asignada para hoy"
            accion={<BotonEnlace a="/clientes/nuevo" variante="secundario"><Icono nombre="tienda" />Dar de alta un cliente</BotonEnlace>}>
            La arma administración. Mientras tanto podés consultar el catálogo o dar de alta comercios nuevos.
          </Vacio>
        </main>
      </div>
    )
  }

  const ruta = datos?.ruta
  const puntos = ruta?.puntos ?? []
  const siguiente = datos?.siguiente
  const fecha = ruta ? new Date(`${ruta.fecha}T00:00:00`).toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long' }) : ''

  return (
    <div className="pantalla">
      <Encabezado titulo="Ruta del día" subtitulo={fecha} accion={accionNuevo} />
      <main className="contenido">
        <Alerta>{error}</Alerta>
        {errorUbicacion && !posicion && <Alerta tipo="aviso">{errorUbicacion}</Alerta>}
        {buscando && !posicion && <Alerta tipo="info">Buscando tu ubicación para optimizar el orden de los puntos…</Alerta>}

        <div className="dos-paneles">
        <section className="panel-mapa">
        <div className="resumen-kpi">
          <div className="kpi"><b>{puntos.length}</b><span>Puntos</span></div>
          <div className="kpi"><b>{ruta?.visitados ?? 0}</b><span>Visitados</span></div>
          <div className="kpi"><b>{ruta?.pendientes ?? 0}</b><span>Pendientes</span></div>
        </div>

        <MapaRuta
          puntos={puntos}
          posicion={posicion}
          trayecto={datos?.trayecto}
          destacado={siguiente?.idPuntoVenta}
          alTocarMapa={marcandoInicio || (!posicion && errorUbicacion) ? tocarMapa : undefined}
        />
        <NotaRecorrido trayecto={datos?.trayecto} criterioOrden={datos?.ruta?.criterioOrden} />

        <div className="grupo-botones">
          <button type="button" className="boton boton-secundario boton-chico" disabled={!posicion || ordenando}
            onClick={() => ordenar(posicion)}>
            <Icono nombre="gps" tamanio={18} />{ordenando ? 'Ordenando…' : 'Reordenar desde acá'}
          </button>
          {manual
            ? <button type="button" className="boton boton-fantasma boton-chico" onClick={volverAlGps}>Usar mi GPS</button>
            : <button type="button" className="boton boton-fantasma boton-chico" onClick={() => setMarcandoInicio((v) => !v)}>
                {marcandoInicio ? 'Tocá el mapa…' : 'Marcar inicio'}
              </button>}
        </div>

        </section>
        <section className="panel-lista">
        {siguiente && (
          <Link to={`/puntos/${siguiente.idPuntoVenta}`} className="item item-destacado">
            <span className="numero" style={{ background: 'var(--dm-magenta)', color: '#fff' }}>{siguiente.orden}</span>
            <span className="item-cuerpo">
              <small style={{ color: 'var(--dm-magenta)', fontWeight: 700 }}>
                SIGUIENTE PUNTO{datos.distanciaSiguienteKm != null ? ` · ${formatoKm(datos.distanciaSiguienteKm)}` : ''}
              </small>
              <strong>{siguiente.nombreFantasia}</strong>
              <small>{siguiente.direccion}</small>
            </span>
            <Icono nombre="flecha" />
          </Link>
        )}
        {!siguiente && puntos.length > 0 && (
          <Alerta tipo="exito">¡Visitaste todos los puntos de hoy!</Alerta>
        )}

        <h2 style={{ fontSize: '1rem' }}>Puntos de la ruta</h2>
        <ul className="lista">
          {puntos.map((p) => (
            <li key={p.idPuntoVenta}>
              <Link to={`/puntos/${p.idPuntoVenta}`} className="item">
                <span className={`numero ${TONO_NUMERO[p.resultado] ?? ''}`}>{p.orden}</span>
                <span className="item-cuerpo">
                  <strong>{p.nombreFantasia}</strong>
                  <small>{p.direccion}</small>
                </span>
                <span className="item-lado"><InsigniaResultado resultado={p.resultado} /></span>
              </Link>
            </li>
          ))}
        </ul>
        </section>
        </div>
      </main>
    </div>
  )
}
