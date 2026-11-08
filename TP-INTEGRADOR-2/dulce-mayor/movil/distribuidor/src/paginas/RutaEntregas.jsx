import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router'
import { esNoEncontrado, mensajeDeError } from '@comun/api/cliente.js'
import { Alerta, Cargando, Encabezado, InsigniaResultado, Vacio } from '@comun/componentes/Interfaz.jsx'
import Icono from '@comun/componentes/Icono.jsx'
import MapaRuta from '@comun/componentes/MapaRuta.jsx'
import NotaRecorrido from '@comun/componentes/NotaRecorrido.jsx'
import useUbicacion from '@comun/hooks/useUbicacion.js'
import { formatoKm, formatoMoneda } from '@comun/utilidades/formato.js'
import { entregas } from '../servicios.js'

const TONO_NUMERO = { ENTREGADO: 'numero-hecho', NO_ENTREGADO: 'numero-error' }
const aCobrar = (punto) => (punto.pedidos ?? []).filter((p) => p.estado === 'PENDIENTE_DE_ENTREGA').reduce((s, p) => s + p.total, 0)

/**
 * Ruta de entregas del día: comercios con pedidos asignados al distribuidor, con el orden optimizado en el
 * backend desde su ubicación (tiempos de manejo de OSRM). Los pedidos no entregados siguen pendientes y vuelven a la ruta del día siguiente.
 */
export default function RutaEntregas() {
  const { state } = useLocation()
  const { posicion, error: errorUbicacion, manual, elegirManual, volverAlGps } = useUbicacion()
  const [datos, setDatos] = useState(null)
  const [sinEntregas, setSinEntregas] = useState(false)
  const [error, setError] = useState(null)
  const [trabajando, setTrabajando] = useState(false)
  const [marcandoInicio, setMarcandoInicio] = useState(false)
  const armadaDesde = useRef(null)

  const armar = useCallback(async (desde) => {
    setTrabajando(true)
    setError(null)
    try {
      setDatos(await entregas.generarRuta(desde))
      setSinEntregas(false)
    } catch (e) {
      if (esNoEncontrado(e)) setSinEntregas(true)
      else setError(mensajeDeError(e, 'No se pudo armar la ruta de entregas.'))
    } finally {
      setTrabajando(false)
    }
  }, [])

  // Se arma con la primera ubicación conocida; si no hay GPS, sin ubicación (orden guardado) hasta que se marque el inicio.
  useEffect(() => {
    const clave = posicion ? (posicion.manual ? `manual:${posicion.latitud},${posicion.longitud}` : 'gps') : (errorUbicacion ? 'sin-gps' : null)
    if (!clave || armadaDesde.current === clave) return
    armadaDesde.current = clave
    armar(posicion)
  }, [posicion, errorUbicacion, armar])

  const tocarMapa = (lat, lon) => { elegirManual(lat, lon); setMarcandoInicio(false) }

  if (sinEntregas) {
    return (
      <div className="pantalla">
        <Encabezado titulo="Mis entregas" />
        <main className="contenido">
          <Alerta tipo="exito">{state?.aviso}</Alerta>
          <Vacio icono="camion" titulo="No tenés entregas pendientes">Cuando administración te asigne pedidos confirmados, van a aparecer acá.</Vacio>
        </main>
      </div>
    )
  }
  if (!datos) {
    return (
      <div className="pantalla">
        <Encabezado titulo="Mis entregas" />
        <main className="contenido">
          <Alerta>{error}</Alerta>
          {!error && <Cargando texto={posicion || errorUbicacion ? 'Armando tu ruta…' : 'Buscando tu ubicación…'} />}
        </main>
      </div>
    )
  }

  const ruta = datos.ruta
  const siguiente = datos.siguiente
  const totalACobrar = ruta.puntos.reduce((s, p) => s + aCobrar(p), 0)

  return (
    <div className="pantalla">
      <Encabezado titulo="Mis entregas" subtitulo={new Date(`${ruta.fecha}T00:00:00`).toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long' })} />
      <main className="contenido">
        <Alerta tipo="exito">{state?.aviso}</Alerta>
        <Alerta>{error}</Alerta>
        {errorUbicacion && !posicion && <Alerta tipo="aviso">{errorUbicacion}</Alerta>}

        <div className="dos-paneles">
        <section className="panel-mapa">
        <div className="resumen-kpi">
          <div className="kpi"><b>{ruta.pendientes}</b><span>Por entregar</span></div>
          <div className="kpi"><b>{ruta.visitados}</b><span>Atendidos</span></div>
          <div className="kpi"><b style={{ fontSize: '1rem', lineHeight: '1.95rem' }}>{formatoMoneda(totalACobrar)}</b><span>A cobrar</span></div>
        </div>

        <MapaRuta puntos={ruta.puntos} posicion={posicion} trayecto={datos.trayecto} destacado={siguiente?.idPuntoVenta}
          alTocarMapa={marcandoInicio || (!posicion && errorUbicacion) ? tocarMapa : undefined} />
        <NotaRecorrido trayecto={datos.trayecto} criterioOrden={ruta.criterioOrden} />
        <div className="grupo-botones">
          <button type="button" className="boton boton-secundario boton-chico" disabled={trabajando} onClick={() => armar(posicion)}>
            <Icono nombre="gps" tamanio={18} />{trabajando ? 'Actualizando…' : 'Actualizar ruta'}
          </button>
          {manual
            ? <button type="button" className="boton boton-fantasma boton-chico" onClick={volverAlGps}>Usar mi GPS</button>
            : <button type="button" className="boton boton-fantasma boton-chico" onClick={() => setMarcandoInicio((v) => !v)}>{marcandoInicio ? 'Tocá el mapa…' : 'Marcar inicio'}</button>}
        </div>

        </section>
        <section className="panel-lista">
        {siguiente
          ? (
            <Link to={`/entregas/${siguiente.idPuntoVenta}`} className="item item-destacado">
              <span className="numero" style={{ background: 'var(--dm-magenta)', color: '#fff' }}>{siguiente.orden}</span>
              <span className="item-cuerpo">
                <small style={{ color: 'var(--dm-magenta)', fontWeight: 700 }}>SIGUIENTE ENTREGA{datos.distanciaSiguienteKm != null ? ` · ${formatoKm(datos.distanciaSiguienteKm)}` : ''}</small>
                <strong>{siguiente.nombreFantasia}</strong>
                <small>{siguiente.direccion}</small>
              </span>
              <Icono nombre="flecha" />
            </Link>
          )
          : <Alerta tipo="exito">Terminaste las entregas de hoy.</Alerta>}

        <h2 style={{ fontSize: '1rem' }}>Paradas</h2>
        <ul className="lista">
          {ruta.puntos.map((p) => (
            <li key={p.idPuntoVenta}>
              <Link to={`/entregas/${p.idPuntoVenta}`} className="item">
                <span className={`numero ${TONO_NUMERO[p.resultado] ?? ''}`}>{p.orden}</span>
                <span className="item-cuerpo">
                  <strong>{p.nombreFantasia}</strong>
                  <small>{p.direccion}</small>
                  <small>{(p.pedidos ?? []).length} {(p.pedidos ?? []).length === 1 ? 'pedido' : 'pedidos'}{aCobrar(p) > 0 ? ` · cobrar ${formatoMoneda(aCobrar(p))}` : ''}</small>
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
