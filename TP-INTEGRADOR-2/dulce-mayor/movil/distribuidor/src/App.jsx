import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './App.css'

const API = import.meta.env.VITE_API_URL || ''
const SESSION_KEY = 'dulce-mayor-distribuidor-session'
const money = (value) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(Number(value) || 0)
const describeError = (error) => (error instanceof Error ? error.message : 'Ocurrió un error inesperado.')

async function api(path, { token, ...options } = {}) {
  const response = await fetch(`${API}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (response.status === 204) return null
  if (!response.ok) {
    const detail = await response.json().catch(() => null)
    throw new Error(detail?.mensaje || detail?.message || `La solicitud falló (${response.status}).`)
  }
  return response.json()
}

function MapaRuta({ paradas, siguienteId }) {
  const contenedor = useRef(null)
  const mapa = useRef(null)
  const capa = useRef(null)

  useEffect(() => {
    if (!contenedor.current || mapa.current) return
    mapa.current = L.map(contenedor.current, { zoomControl: true, scrollWheelZoom: false }).setView([-32.89, -68.84], 12)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(mapa.current)
    capa.current = L.layerGroup().addTo(mapa.current)
    return () => {
      mapa.current?.remove()
      mapa.current = null
    }
  }, [])

  useEffect(() => {
    const grupo = capa.current
    const instancia = mapa.current
    if (!grupo || !instancia) return
    grupo.clearLayers()
    const puntos = (paradas || []).filter((p) => p.latitud != null && p.longitud != null)
    puntos.forEach((parada) => {
      const marcador = L.marker([parada.latitud, parada.longitud])
      marcador.bindPopup(
        `<strong>${parada.orden}. ${parada.nombreComercial}</strong><br>${parada.calle} ${parada.numeracion}`
        + `<br>${parada.estado}${parada.id === siguienteId ? ' · siguiente' : ''}`,
      )
      marcador.addTo(grupo)
    })
    if (puntos.length > 0) {
      instancia.fitBounds(L.latLngBounds(puntos.map((p) => [p.latitud, p.longitud])), { padding: [24, 24] })
    }
  }, [paradas, siguienteId])

  return <div className="mapa" ref={contenedor} role="img" aria-label="Mapa de la hoja de ruta" />
}

function App() {
  const [session, setSession] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null') } catch { return null }
  })
  const [login, setLogin] = useState({ correo: '', clave: '' })
  const [ruta, setRuta] = useState(null)
  const [seleccion, setSeleccion] = useState(null)
  const [motivo, setMotivo] = useState('')
  const [cargando, setCargando] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const cargarRuta = useCallback(async () => {
    if (!session?.token) return
    setCargando(true)
    setError('')
    try {
      const propia = await api('/api/distribuidor/ruta', { token: session.token })
      setRuta(propia)
      if (propia?.idSiguienteParada) {
        setSeleccion((actual) => propia.paradas.find((p) => p.id === actual?.id)
          || propia.paradas.find((p) => p.id === propia.idSiguienteParada))
      }
    } catch (requestError) {
      setError(describeError(requestError))
    } finally {
      setCargando(false)
    }
  }, [session])

  useEffect(() => { queueMicrotask(cargarRuta) }, [cargarRuta])

  const paradas = useMemo(() => ruta?.paradas || [], [ruta])
  const pendientes = useMemo(() => paradas.filter((p) => p.estado === 'PENDIENTE'), [paradas])

  async function iniciarSesion(event) {
    event.preventDefault()
    setGuardando(true)
    setError('')
    try {
      const sesion = await api('/api/auth/login', { method: 'POST', body: JSON.stringify(login) })
      if (sesion.usuario?.debeCambiarClave) {
        throw new Error('La cuenta tiene una clave temporal: cambiala desde el panel de administración antes de usar la app.')
      }
      if (sesion.usuario?.rol !== 'DISTRIBUIDOR') {
        throw new Error('Esta aplicación es solo para distribuidores.')
      }
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(sesion))
      setSession(sesion)
      setLogin({ correo: '', clave: '' })
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  function cerrarSesion() {
    sessionStorage.removeItem(SESSION_KEY)
    setSession(null)
    setRuta(null)
    setSeleccion(null)
    setAviso('')
    setError('')
  }

  async function resolverParada(parada, accion) {
    if (!parada) { setError('Elegí una parada.'); return }
    if (accion === 'fallar' && !motivo.trim()) { setError('Indicá el motivo del intento fallido.'); return }
    setGuardando(true)
    setError('')
    setAviso('')
    try {
      const cuerpo = accion === 'fallar' ? { motivo: motivo.trim() } : {}
      const actualizada = await api(`/api/distribuidor/paradas/${parada.id}/${accion}`, {
        token: session.token, method: 'POST', body: JSON.stringify(cuerpo),
      })
      setRuta(actualizada)
      setMotivo('')
      const resuelta = actualizada.paradas.find((p) => p.id === parada.id)
      setSeleccion(null)
      setAviso(accion === 'entregar'
        ? `Entrega registrada${resuelta?.cobroEfectivo ? ' con cobro en efectivo' : ''}.`
        : 'Intento fallido registrado. El pedido queda pendiente de reprogramación.')
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  if (!session) {
    return (
      <main className="login-shell">
        <form className="login-card" onSubmit={iniciarSesion}>
          <div className="marca">DM</div>
          <p className="eyebrow">DULCE MAYOR · LOGÍSTICA</p>
          <h1>App Distribuidor</h1>
          <p className="muted">Ingresá con tu cuenta de reparto para ver la hoja de ruta del día.</p>
          {error && <div className="alerta" role="alert">{error}</div>}
          <label>Correo<input type="email" required value={login.correo} onChange={(e) => setLogin({ ...login, correo: e.target.value })} /></label>
          <label>Contraseña<input type="password" required value={login.clave} onChange={(e) => setLogin({ ...login, clave: e.target.value })} /></label>
          <button className="button primary full" disabled={guardando}>{guardando ? 'Ingresando…' : 'Ingresar'}</button>
        </form>
      </main>
    )
  }

  return (
    <div className="app">
      <header className="topbar">
        <div><strong>Hoja de ruta</strong><small>{session.usuario?.nombre || session.usuario?.correo}</small></div>
        <button type="button" className="button secondary" onClick={cerrarSesion}>Salir</button>
      </header>

      <main className="contenido">
        {error && <div className="alerta" role="alert">{error}</div>}
        {aviso && <div className="aviso">{aviso}</div>}

        {cargando && !ruta && <p className="muted">Cargando la hoja de ruta…</p>}

        {!cargando && !ruta && (
          <section className="panel vacio">
            <h2>Todavía no tenés una hoja de ruta</h2>
            <p className="muted">Cuando administración asigne pedidos a tu jornada, van a aparecer acá.</p>
            <button type="button" className="button secondary" onClick={cargarRuta}>Actualizar</button>
          </section>
        )}

        {ruta && (
          <>
            <section className="panel resumen">
              <div>
                <h2>{ruta.fecha}</h2>
                <p className="muted">
                  {ruta.estado} · {ruta.vehiculo || 'sin vehículo'} {ruta.patente || ''} · {ruta.paradasPendientes} pendientes
                </p>
              </div>
              <div className="resumen-datos">
                <span><small>Paradas</small><strong>{paradas.length}</strong></span>
                <span><small>Cobrado</small><strong>{money(ruta.totalCobrado)}</strong></span>
                <button type="button" className="button secondary" onClick={cargarRuta} disabled={cargando}>Actualizar</button>
              </div>
            </section>

            <MapaRuta paradas={paradas} siguienteId={ruta.idSiguienteParada} />

            <section className="panel">
              <div className="panel-heading"><h2>Paradas</h2><span className="badge">{pendientes.length} pendientes</span></div>
              <div className="paradas">
                {paradas.map((parada) => (
                  <article key={parada.id}
                           className={`parada${parada.id === ruta.idSiguienteParada ? ' siguiente' : ''}${seleccion?.id === parada.id ? ' activa' : ''}`}>
                    <div className="parada-orden">{parada.orden}</div>
                    <div className="parada-datos">
                      <strong>{parada.nombreComercial}</strong>
                      <small>{parada.calle} {parada.numeracion}{parada.localidad ? ` · ${parada.localidad}` : ''}</small>
                      <small>{money(parada.total)} · pedido {parada.estadoPedido}</small>
                      {parada.motivoFallo && <small className="fallo">Motivo: {parada.motivoFallo}</small>}
                      {parada.cobroEfectivo && <small className="ok">Cobrado en efectivo</small>}
                    </div>
                    <div className="parada-estado">
                      <span className={`chip chip-${String(parada.estado).toLowerCase()}`}>{parada.estado}</span>
                      {parada.estado === 'PENDIENTE' && (
                        <button type="button" className="button secondary"
                                onClick={() => { setSeleccion(parada); setError('') }}>
                          {seleccion?.id === parada.id ? 'Elegida' : 'Elegir'}
                        </button>
                      )}
                      {parada.latitud != null && parada.longitud != null && (
                        <a className="button secondary" target="_blank" rel="noreferrer"
                           href={`https://www.openstreetmap.org/?mlat=${parada.latitud}&mlon=${parada.longitud}#map=17/${parada.latitud}/${parada.longitud}`}>
                          Mapa
                        </a>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            </section>

            {seleccion && (
              <section className="panel accion">
                <h2>{seleccion.nombreComercial}</h2>
                <p className="muted">{seleccion.calle} {seleccion.numeracion} · {money(seleccion.total)}</p>
                <button type="button" className="button primary full" disabled={guardando}
                        onClick={() => resolverParada(seleccion, 'entregar')}>
                  {guardando ? 'Registrando…' : 'Registrar entrega'}
                </button>
                <label>Motivo del intento fallido
                  <input type="text" maxLength={200} value={motivo}
                         onChange={(e) => setMotivo(e.target.value)}
                         placeholder="Comercio cerrado, dirección incorrecta…" />
                </label>
                <button type="button" className="button secondary full" disabled={guardando}
                        onClick={() => resolverParada(seleccion, 'fallar')}>
                  Registrar intento fallido
                </button>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}

export default App
