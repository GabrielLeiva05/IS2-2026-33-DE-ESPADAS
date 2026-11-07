import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './App.css'

const API = import.meta.env.VITE_API_URL || ''
const SESSION_KEY = 'dulce-mayor-preventista-session'
const PENDING_ORDER_KEY = `${SESSION_KEY}-pending-order`
const money = (value) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(Number(value) || 0)
const medioLegible = (medio) => ({ TRANSFERENCIA: 'Transferencia', MERCADO_PAGO: 'Mercado Pago', EFECTIVO: 'Efectivo' }[medio] || medio)

/**
 * Mapa de OpenStreetMap (Leaflet) con los puntos de la cartera o del plan del día.
 * Si se pasa onClick, un toque en el mapa devuelve las coordenadas elegidas (pin manual).
 */
function MapaPuntos({ puntos, onClick }) {
  const contenedor = useRef(null)
  const mapa = useRef(null)
  const capa = useRef(null)
  const clickRef = useRef(onClick)

  useEffect(() => { clickRef.current = onClick }, [onClick])

  useEffect(() => {
    if (!contenedor.current || mapa.current) return
    mapa.current = L.map(contenedor.current, { scrollWheelZoom: false }).setView([-32.89, -68.84], 12)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(mapa.current)
    capa.current = L.layerGroup().addTo(mapa.current)
    const manejador = (evento) => clickRef.current?.(evento.latlng.lat, evento.latlng.lng)
    mapa.current.on('click', manejador)
    return () => {
      mapa.current?.off('click', manejador)
      mapa.current?.remove()
      mapa.current = null
    }
  }, [])

  useEffect(() => {
    const grupo = capa.current
    const instancia = mapa.current
    if (!grupo || !instancia) return
    grupo.clearLayers()
    const validos = (puntos || []).filter((punto) => punto.latitud != null && punto.longitud != null)
    validos.forEach((punto) => {
      L.marker([punto.latitud, punto.longitud])
        .bindPopup(`<strong>${punto.orden ?? ''} ${punto.nombreComercial ?? 'Comercio'}</strong>`)
        .addTo(grupo)
    })
    if (validos.length > 0) {
      instancia.fitBounds(L.latLngBounds(validos.map((punto) => [punto.latitud, punto.longitud])), { padding: [24, 24] })
    }
  }, [puntos])

  return <div ref={contenedor} role="img" aria-label="Mapa de comercios"
              style={{ height: 260, borderRadius: 14, overflow: 'hidden', marginBottom: 16 }} />
}
const describeError = (error) => error instanceof Error ? error.message : 'Ocurrió un error inesperado.'

async function api(path, { token, ...options } = {}) {
  const response = await fetch(`${API}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (!response.ok) {
    const detail = await response.json().catch(() => null)
    throw new Error(detail?.mensaje || detail?.message || `La solicitud falló (${response.status}).`)
  }
  if (response.status === 204) return null
  return response.json()
}

function App() {
  const [session, setSession] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null') } catch { return null }
  })
  const [screen, setScreen] = useState('cartera')
  const [cartera, setCartera] = useState([])
  const [catalogo, setCatalogo] = useState([])
  const [pedidos, setPedidos] = useState([])
  const [puntoId, setPuntoId] = useState('')
  const [pedidoPendienteId, setPedidoPendienteId] = useState(() => sessionStorage.getItem(PENDING_ORDER_KEY) || '')
  const [cantidades, setCantidades] = useState({})
  const [busqueda, setBusqueda] = useState('')
  const [codigoPromo, setCodigoPromo] = useState('')
  const [pedidoPendiente, setPedidoPendiente] = useState(null)
  const [login, setLogin] = useState({ correo: '', clave: '' })
  const [nuevoPunto, setNuevoPunto] = useState({ nombreCliente: '', apellidoCliente: '', correoContactoCliente: '', tipoDocumentoCliente: 'DNI', numeroDocumentoCliente: '', nombreComercial: '', telefono: '', emailContacto: '', calle: '', numeracion: '', barrio: '', referencia: '', latitud: '', longitud: '', idLocalidad: '' })
  const [localidades, setLocalidades] = useState([])
  const [cargando, setCargando] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  const [cobroPedido, setCobroPedido] = useState(null)
  const [pagosDelPedido, setPagosDelPedido] = useState([])
  const [plan, setPlan] = useState({ fecha: '', visitas: [] })
  const [planCargando, setPlanCargando] = useState(false)

  const cargarDatos = useCallback(async () => {
    if (!session?.token) return
    setCargando(true)
    setError('')
    try {
      const [puntos, productos, misPedidos, departamentosApi] = await Promise.all([
        api('/api/preventista/puntos-venta', { token: session.token }),
        api('/api/catalogo', { token: session.token }),
        api('/api/preventista/pedidos', { token: session.token }),
        api('/api/departamentos', { token: session.token }),
      ])
      const localidadesApi = (await Promise.all(departamentosApi.map((departamento) => api(`/api/localidades/departamento/${departamento.id}`, { token: session.token })))).flat()
      setCartera(puntos)
      setCatalogo(productos)
      setPedidos(misPedidos)
      const pedidoPendienteActual = misPedidos.find((pedido) => pedido.id === pedidoPendienteId)
      setPedidoPendiente(pedidoPendienteActual || null)
      if (pedidoPendienteId && (!pedidoPendienteActual || pedidoPendienteActual.estado !== 'BORRADOR')) {
        sessionStorage.removeItem(PENDING_ORDER_KEY)
        setPedidoPendienteId('')
        if (pedidoPendienteActual) setCantidades({})
      }
      setLocalidades(localidadesApi)
      setPuntoId((actual) => puntos.some((punto) => punto.id === actual) ? actual : puntos[0]?.id || '')
    } catch (requestError) {
      setError(describeError(requestError))
    } finally {
      setCargando(false)
    }
  }, [session, pedidoPendienteId])

  useEffect(() => { queueMicrotask(cargarDatos) }, [cargarDatos])

  const productosFiltrados = useMemo(() => catalogo.filter((producto) =>
    `${producto.nombre} ${producto.codigo} ${producto.categoria} ${producto.subCategoria}`.toLowerCase().includes(busqueda.toLowerCase())), [catalogo, busqueda])
  const lineas = useMemo(() => catalogo.filter((producto) => Number(cantidades[producto.id]) > 0)
    .map((producto) => ({ idProducto: producto.id, cantidadPresentaciones: Number(cantidades[producto.id]) })), [catalogo, cantidades])
  const totalBorrador = useMemo(() => catalogo.reduce((total, producto) =>
    total + (Number(cantidades[producto.id]) || 0) * Number(producto.precio || 0), 0), [catalogo, cantidades])

  async function iniciarSesion(event) {
    event.preventDefault()
    setGuardando(true)
    setError('')
    try {
      const sesion = await api('/api/auth/login', { method: 'POST', body: JSON.stringify(login) })
      if (sesion.usuario?.rol !== 'PREVENTISTA') throw new Error('La cuenta no tiene permisos para ingresar a la app preventista.')
      if (sesion.usuario.debeCambiarClave) throw new Error('La cuenta debe cambiar su contraseña antes de continuar. Ingresá desde el portal web.')
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(sesion))
      setSession(sesion)
      setAviso('Ingresaste correctamente.')
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  function cerrarSesion() {
    sessionStorage.removeItem(SESSION_KEY)
    sessionStorage.removeItem(PENDING_ORDER_KEY)
    setPedidoPendienteId('')
    setPedidoPendiente(null)
    setSession(null)
    setCartera([])
    setPedidos([])
    setAviso('')
    setError('')
  }

  async function guardarPunto(event) {
    event.preventDefault()
    setGuardando(true)
    setError('')
    setAviso('')
    try {
      const payload = {
        nombreCliente: nuevoPunto.nombreCliente,
        apellidoCliente: nuevoPunto.apellidoCliente,
        correoContactoCliente: nuevoPunto.correoContactoCliente,
        tipoDocumentoCliente: nuevoPunto.tipoDocumentoCliente || null,
        numeroDocumentoCliente: nuevoPunto.numeroDocumentoCliente || null,
        nombreComercial: nuevoPunto.nombreComercial,
        telefono: nuevoPunto.telefono || null,
        emailContacto: nuevoPunto.emailContacto || null,
        direccion: {
          calle: nuevoPunto.calle,
          numeracion: nuevoPunto.numeracion,
          barrio: nuevoPunto.barrio || null,
          referencia: nuevoPunto.referencia || null,
          latitud: nuevoPunto.latitud === '' ? null : Number(nuevoPunto.latitud),
          longitud: nuevoPunto.longitud === '' ? null : Number(nuevoPunto.longitud),
          idLocalidad: nuevoPunto.idLocalidad,
        },
      }
      await api('/api/preventista/puntos-venta', { token: session.token, method: 'POST', body: JSON.stringify(payload) })
      setNuevoPunto({ nombreCliente: '', apellidoCliente: '', correoContactoCliente: '', tipoDocumentoCliente: 'DNI', numeroDocumentoCliente: '', nombreComercial: '', telefono: '', emailContacto: '', calle: '', numeracion: '', barrio: '', referencia: '', latitud: '', longitud: '', idLocalidad: '' })
      setAviso('Punto de venta registrado en la cartera.')
      await cargarDatos()
      setScreen('cartera')
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  async function guardarPedido(event) {
    event.preventDefault()
    if (!puntoId || lineas.length === 0) { setError('Elegí un comercio y al menos un producto para continuar.'); return }
    setGuardando(true)
    setError('')
    setAviso('')
    try {
      const pedido = pedidoPendienteId
        ? await api(`/api/preventista/pedidos/${pedidoPendienteId}/lineas`, { token: session.token, method: 'PUT', body: JSON.stringify({ idPuntoVenta: puntoId, lineas }) })
        : await api('/api/preventista/pedidos', { token: session.token, method: 'POST', body: JSON.stringify({ idPuntoVenta: puntoId, lineas }) })
      sessionStorage.setItem(PENDING_ORDER_KEY, pedido.id)
      setPedidoPendienteId(pedido.id)
      let detallePromo = ''
      const codigo = codigoPromo.trim()
      if (codigo) {
        const conPromocion = await api(`/api/preventista/pedidos/${pedido.id}/promocion`, { token: session.token, method: 'POST', body: JSON.stringify({ codigo }) })
        detallePromo = ` Promoción ${conPromocion.promocionCodigo}: ${money(conPromocion.descuento)} de descuento.`
      }
      await confirmarPedido(pedido.id, detallePromo)
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  function ajustarPedidoPendiente() {
    if (!pedidoPendiente || pedidoPendiente.estado !== 'BORRADOR') {
      setError('Actualizá los datos para verificar el estado del pedido antes de editarlo.')
      return
    }
    setPuntoId(pedidoPendiente.idPuntoVenta)
    setCantidades(Object.fromEntries(pedidoPendiente.lineas.map((linea) => [linea.idProducto, String(linea.cantidadPresentaciones)])))
    setScreen('pedido')
    setError('')
  }

  async function confirmarPedido(idPedido = pedidoPendienteId, detallePromo = '') {
    if (!idPedido) return
    setGuardando(true)
    setError('')
    try {
      await api(`/api/preventista/pedidos/${idPedido}/confirmar`, { token: session.token, method: 'POST' })
      sessionStorage.removeItem(PENDING_ORDER_KEY)
      setPedidoPendienteId('')
      setPedidoPendiente(null)
      const pedidosActualizados = await api('/api/preventista/pedidos', { token: session.token })
      setPedidos(pedidosActualizados)
      setCantidades({})
      setCodigoPromo('')
      setAviso('Pedido confirmado y reserva de stock realizada.' + detallePromo)
      await cargarDatos()
      setScreen('pedidos')
    } catch (requestError) {
      setError(describeError(requestError))
      await cargarDatos()
    } finally { setGuardando(false) }
  }

  async function cargarPlan() {
    setPlanCargando(true)
    setError('')
    try {
      setPlan(await api('/api/preventista/visitas', { token: session.token }))
    } catch (requestError) { setError(describeError(requestError)) } finally { setPlanCargando(false) }
  }

  async function generarPlan() {
    setPlanCargando(true)
    setError('')
    setAviso('')
    try {
      setPlan(await api('/api/preventista/visitas/generar', { token: session.token, method: 'POST' }))
      setAviso('Planificación del día generada por cercanía. Podés reordenarla.')
    } catch (requestError) { setError(describeError(requestError)) } finally { setPlanCargando(false) }
  }

  async function moverVisita(indice, salto) {
    const destino = indice + salto
    if (destino < 0 || destino >= plan.visitas.length) return
    const reordenadas = [...plan.visitas]
    const [movida] = reordenadas.splice(indice, 1)
    reordenadas.splice(destino, 0, movida)
    setPlanCargando(true)
    setError('')
    try {
      setPlan(await api('/api/preventista/visitas/orden', {
        token: session.token, method: 'PUT',
        body: JSON.stringify({ idsVisitas: reordenadas.map((visita) => visita.id) }),
      }))
    } catch (requestError) { setError(describeError(requestError)) } finally { setPlanCargando(false) }
  }

  async function abrirCobro(pedido) {
    setError('')
    setAviso('')
    setCobroPedido(pedido)
    setPagosDelPedido([])
    setScreen('cobro')
    try {
      setPagosDelPedido(await api(`/api/preventista/pedidos/${pedido.id}/pagos`, { token: session.token }))
    } catch (requestError) { setError(describeError(requestError)) }
  }

  async function iniciarCobro(medio) {
    if (!cobroPedido) return
    setGuardando(true)
    setError('')
    setAviso('')
    try {
      const pago = await api(`/api/preventista/pedidos/${cobroPedido.id}/pagos`, { token: session.token, method: 'POST', body: JSON.stringify({ medio }) })
      setPagosDelPedido((actuales) => [pago, ...actuales])
      setAviso(`${medioLegible(medio)}: cobro iniciado. El pedido se completa cuando administración acredita el pago.`)
    } catch (requestError) { setError(describeError(requestError)) } finally { setGuardando(false) }
  }

  async function descargarComprobante(idPedido) {
    setError('')
    try {
      const respuesta = await fetch(`${API}/api/preventista/pedidos/${idPedido}/comprobante`, { headers: { Authorization: `Bearer ${session.token}` } })
      if (!respuesta.ok) {
        const detalle = await respuesta.json().catch(() => null)
        throw new Error(detalle?.mensaje || `No se pudo descargar el comprobante (${respuesta.status}).`)
      }
      const url = URL.createObjectURL(await respuesta.blob())
      const enlace = document.createElement('a')
      enlace.href = url
      enlace.download = `comprobante-${idPedido}.html`
      enlace.click()
      URL.revokeObjectURL(url)
    } catch (requestError) { setError(describeError(requestError)) }
  }

  if (!session) return (
    <main className="login-shell">
      <form className="login-card" onSubmit={iniciarSesion}>
        <div className="brand-mark">DM</div>
        <p className="eyebrow">DULCE MAYOR · EQUIPO COMERCIAL</p>
        <h1>App Preventista</h1>
        <p className="muted">Ingresá para gestionar tu cartera y registrar pedidos.</p>
        {error && <p className="notice error" role="alert">{error}</p>}
        <label>Correo electrónico<input type="email" autoComplete="username" required value={login.correo} onChange={(event) => setLogin({ ...login, correo: event.target.value })} /></label>
        <label>Contraseña<input type="password" autoComplete="current-password" required value={login.clave} onChange={(event) => setLogin({ ...login, clave: event.target.value })} /></label>
        <button className="button primary full" disabled={guardando}>{guardando ? 'Ingresando…' : 'Ingresar a mi cuenta'}</button>
        <p className="login-foot">Acceso exclusivo para personal preventista.</p>
      </form>
    </main>
  )

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <a className="brand" href="#inicio" onClick={() => setScreen('cartera')}><span className="brand-mark">DM</span><span>Dulce Mayor<small>Fuerza de ventas</small></span></a>
        <p className="nav-caption">GESTIÓN COMERCIAL</p>
        <nav aria-label="Navegación principal">
          {[['cartera', 'Cartera', '▦'], ['nuevo-punto', 'Nuevo comercio', '+'], ['pedido', 'Nuevo pedido', '＋'], ['pedidos', 'Pedidos', '≡']].map(([id, titulo, icono]) => <button key={id} className={`nav-link ${screen === id ? 'active' : ''}`} onClick={() => { setScreen(id); setError(''); setAviso('') }}><span>{icono}</span>{titulo}{id === 'pedidos' && pedidos.length > 0 && <b>{pedidos.length}</b>}</button>)}
        </nav>
        <div className="sidebar-footer"><div className="avatar">{session.usuario?.nombre?.[0] || 'P'}</div><div className="profile"><strong>{session.usuario?.nombre || 'Preventista'} {session.usuario?.apellido || ''}</strong><small>{session.usuario?.correo}</small></div><button className="logout" aria-label="Cerrar sesión" title="Cerrar sesión" onClick={cerrarSesion}>↪</button></div>
      </aside>
      <main className="main-content">
        <header className="topbar"><div><p className="eyebrow">{new Intl.DateTimeFormat('es-AR', { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date())}</p><strong>Hola, {session.usuario?.nombre || 'equipo comercial'}</strong></div><button className="icon-button" aria-label="Actualizar datos" title="Actualizar datos" onClick={cargarDatos} disabled={cargando}>↻</button></header>
        <section className="page-content">
          {error && <p className="notice error" role="alert">{error}<button aria-label="Cerrar aviso" onClick={() => setError('')}>×</button></p>}
          {aviso && <p className="notice success" role="status">{aviso}<button aria-label="Cerrar aviso" onClick={() => setAviso('')}>×</button></p>}
          {pedidoPendienteId && <div className="notice info" role="alert"><span>La confirmación no se completó. Revisá el estado; si sigue en borrador, podés ajustar el carrito y volver a confirmar.</span><button type="button" disabled={guardando || cargando || pedidoPendiente?.estado !== 'BORRADOR'} onClick={ajustarPedidoPendiente}>Ajustar pedido</button></div>}
          {cargando && <p className="loading" role="status">Sincronizando con Dulce Mayor…</p>}
          {screen === 'cartera' && <>
            <div className="page-heading"><div><p className="eyebrow">TU OPERACIÓN</p><h1>Cartera de clientes</h1><p className="muted">Comercios asignados a tu zona de trabajo.</p></div><div className="heading-actions"><button type="button" className="button secondary" onClick={() => { setScreen('ruta'); cargarPlan() }}>Planificar ruta</button><button className="button primary" onClick={() => setScreen('nuevo-punto')}>＋ Nuevo comercio</button></div></div>
            <div className="stats-grid"><article className="stat-card"><span>Comercios activos</span><strong>{cartera.length}</strong><small>En tu cartera</small></article><article className="stat-card"><span>Pedidos registrados</span><strong>{pedidos.length}</strong><small>Historial de ventas</small></article><article className="stat-card"><span>Productos disponibles</span><strong>{catalogo.length}</strong><small>Con stock y precio vigente</small></article></div>
            <section className="panel"><div className="panel-heading"><div><h2>Comercios asignados</h2><p>Datos de contacto y ubicación</p></div><span className="count-badge">{cartera.length} comercios</span></div>
              {cartera.length === 0 && !cargando ? <div className="empty-state"><span>⌖</span><h3>Tu cartera empieza acá</h3><p>Registrá un comercio para poder cargar su próximo pedido.</p><button className="button secondary" onClick={() => setScreen('nuevo-punto')}>Registrar comercio</button></div> : <div className="table-wrap"><table><thead><tr><th>COMERCIO</th><th>RESPONSABLE</th><th>CONTACTO</th><th>LOCALIDAD</th><th></th></tr></thead><tbody>{cartera.map((punto) => <tr key={punto.id}><td><strong>{punto.nombreComercial}</strong><small className="table-sub">{punto.direccion?.calle} {punto.direccion?.numeracion}</small></td><td>{punto.nombreCliente} {punto.apellidoCliente}</td><td>{punto.telefono || punto.emailContacto || '—'}</td><td>{punto.direccion?.localidad || '—'}</td><td><button className="button tiny" onClick={() => { setPuntoId(punto.id); setScreen('pedido') }}>Cargar pedido</button></td></tr>)}</tbody></table></div>}
            </section>
          </>}
          {screen === 'nuevo-punto' && <>
            <div className="page-heading"><div><p className="eyebrow">CARTERA DE CLIENTES</p><h1>Registrar comercio</h1><p className="muted">El punto quedará asignado automáticamente a tu cartera.</p></div></div>
            <form className="panel form-panel" onSubmit={guardarPunto}>
              <p className="notice info">La ubicación es obligatoria para registrar el punto. Ingresá las coordenadas del comercio.</p>
              <div className="form-section"><h2>Responsable del comercio</h2><p>Datos de contacto de la persona titular.</p><div className="form-grid"><label>Nombre<input required value={nuevoPunto.nombreCliente} onChange={(e) => setNuevoPunto({ ...nuevoPunto, nombreCliente: e.target.value })} /></label><label>Apellido<input required value={nuevoPunto.apellidoCliente} onChange={(e) => setNuevoPunto({ ...nuevoPunto, apellidoCliente: e.target.value })} /></label><label>Correo de contacto<input type="email" required value={nuevoPunto.correoContactoCliente} onChange={(e) => setNuevoPunto({ ...nuevoPunto, correoContactoCliente: e.target.value })} /></label><label>Tipo de documento<select value={nuevoPunto.tipoDocumentoCliente} onChange={(e) => setNuevoPunto({ ...nuevoPunto, tipoDocumentoCliente: e.target.value })}><option>DNI</option><option>CUIL</option><option>CUIT</option><option>PASAPORTE</option><option value="">Sin dato</option></select></label><label>Número de documento<input value={nuevoPunto.numeroDocumentoCliente} onChange={(e) => setNuevoPunto({ ...nuevoPunto, numeroDocumentoCliente: e.target.value })} /></label></div></div>
              <div className="form-section"><h2>Datos del comercio</h2><p>Información para identificar y contactar el punto de venta.</p><div className="form-grid"><label className="wide-field">Nombre comercial<input required value={nuevoPunto.nombreComercial} onChange={(e) => setNuevoPunto({ ...nuevoPunto, nombreComercial: e.target.value })} /></label><label>Teléfono<input type="tel" value={nuevoPunto.telefono} onChange={(e) => setNuevoPunto({ ...nuevoPunto, telefono: e.target.value })} /></label><label>Correo del comercio<input type="email" value={nuevoPunto.emailContacto} onChange={(e) => setNuevoPunto({ ...nuevoPunto, emailContacto: e.target.value })} /></label></div></div>
              <div className="form-section"><h2>Dirección y ubicación</h2><p>La ubicación ayuda a planificar la visita.</p><div className="form-grid"><label>Calle<input required value={nuevoPunto.calle} onChange={(e) => setNuevoPunto({ ...nuevoPunto, calle: e.target.value })} /></label><label>Altura<input required value={nuevoPunto.numeracion} onChange={(e) => setNuevoPunto({ ...nuevoPunto, numeracion: e.target.value })} /></label><label>Barrio<input value={nuevoPunto.barrio} onChange={(e) => setNuevoPunto({ ...nuevoPunto, barrio: e.target.value })} /></label><label>Localidad<select required value={nuevoPunto.idLocalidad} onChange={(e) => setNuevoPunto({ ...nuevoPunto, idLocalidad: e.target.value })}><option value="">Seleccionar localidad</option>{localidades.map((localidad) => <option key={localidad.id} value={localidad.id}>{localidad.nombre}{localidad.codigoPostal ? ` · CP ${localidad.codigoPostal}` : ''}</option>)}</select></label><label>Latitud<input type="number" step="any" required value={nuevoPunto.latitud} onChange={(e) => setNuevoPunto({ ...nuevoPunto, latitud: e.target.value })} /></label><label>Longitud<input type="number" step="any" required value={nuevoPunto.longitud} onChange={(e) => setNuevoPunto({ ...nuevoPunto, longitud: e.target.value })} /></label><label className="wide-field">Referencias<input value={nuevoPunto.referencia} onChange={(e) => setNuevoPunto({ ...nuevoPunto, referencia: e.target.value })} /></label></div><p className="muted mb-0">Tocá el mapa para marcar la ubicación exacta del comercio (pin manual).</p><MapaPuntos puntos={nuevoPunto.latitud && nuevoPunto.longitud ? [{ latitud: Number(nuevoPunto.latitud), longitud: Number(nuevoPunto.longitud), nombreComercial: nuevoPunto.nombreComercial || 'Comercio' }] : []} onClick={(latitud, longitud) => setNuevoPunto({ ...nuevoPunto, latitud: latitud.toFixed(6), longitud: longitud.toFixed(6) })} /></div>
              <div className="form-actions"><button type="button" className="button secondary" onClick={() => setScreen('cartera')}>Cancelar</button><button className="button primary" disabled={guardando}>{guardando ? 'Guardando…' : 'Guardar comercio'}</button></div>
            </form>
          </>}
          {screen === 'pedido' && <>
            <div className="page-heading"><div><p className="eyebrow">VENTAS</p><h1>Nuevo pedido</h1><p className="muted">Ajustá el carrito y revisá el total antes de reservar stock.</p></div></div>
            {cartera.length === 0 ? <section className="panel empty-state"><span>⌖</span><h3>Primero necesitás un comercio</h3><p>Registrá el punto de venta antes de cargar un pedido.</p><button type="button" className="button primary" onClick={() => setScreen('nuevo-punto')}>Registrar comercio</button></section> : <form className="order-layout" onSubmit={guardarPedido}><section className="panel order-products"><label className="search-label">Buscar producto<input type="search" placeholder="Nombre, categoría o código…" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} /></label><div className="product-list">{productosFiltrados.map((producto) => <article className="product-row" key={producto.id}><div className="product-art">{producto.idsImagen?.[0] ? <img src={`${API}/api/imagenes/${producto.idsImagen[0]}/contenido`} alt="" /> : 'DM'}</div><div className="product-info"><strong>{producto.nombre}</strong><small>{producto.codigo} · {producto.presentacion} · {producto.unidadesPorPresentacion} unidades</small><span>{money(producto.precio)} <small>/ presentación</small></span><em>{producto.stockDisponible} unidades disponibles</em></div><label className="quantity-control">Presentaciones<input type="number" min="0" max="999" step="1" inputMode="numeric" value={cantidades[producto.id] || ''} onChange={(e) => setCantidades({ ...cantidades, [producto.id]: e.target.value })} aria-label={`Cantidad de ${producto.nombre}`} /></label></article>)}{productosFiltrados.length === 0 && <div className="empty-state"><h3>No hay productos para mostrar</h3><p>Probá con otro término de búsqueda.</p></div>}</div></section>
              <aside className="panel order-summary"><h2>Resumen</h2><label>Comercio<select required value={puntoId} onChange={(e) => setPuntoId(e.target.value)}><option value="">Seleccionar comercio</option>{cartera.map((punto) => <option key={punto.id} value={punto.id}>{punto.nombreComercial}</option>)}</select></label><div className="summary-line"><span>Productos</span><strong>{lineas.length}</strong></div><label>Código promocional<input type="text" value={codigoPromo} onChange={(e) => setCodigoPromo(e.target.value)} placeholder="Opcional" /></label>{pedidoPendiente?.descuento > 0 && <div className="summary-line"><span>Descuento {pedidoPendiente.promocionCodigo}</span><strong>−{money(pedidoPendiente.descuento)}</strong></div>}<div className="summary-total"><span>Total estimado</span><strong>{money(totalBorrador)}</strong></div><p className="summary-hint">El servidor recalcula precios y verifica stock al confirmar. Podés poner una cantidad en cero para quitar ese producto.</p><button className="button primary full" disabled={guardando || lineas.length === 0}>{guardando ? (pedidoPendienteId ? 'Actualizando y confirmando…' : 'Confirmando…') : 'Confirmar pedido'}</button><button type="button" className="button secondary full" onClick={() => setCantidades({})}>Vaciar selección</button></aside>
            </form>}
          </>}
          {screen === 'pedidos' && <><div className="page-heading"><div><p className="eyebrow">ACTIVIDAD</p><h1>Pedidos</h1><p className="muted">Seguimiento de los pedidos de tu cartera.</p></div><button className="button primary" onClick={() => setScreen('pedido')}>＋ Nuevo pedido</button></div><section className="panel"><div className="panel-heading"><div><h2>Historial de pedidos</h2><p>Más recientes primero</p></div><span className="count-badge">{pedidos.length} pedidos</span></div>{pedidos.length === 0 ? <div className="empty-state"><span>≡</span><h3>Todavía no hay pedidos</h3><p>Cuando registres un pedido, lo vas a encontrar acá.</p></div> : <div className="table-wrap"><table><thead><tr><th>COMERCIO</th><th>FECHA</th><th>PRODUCTOS</th><th>ESTADO</th><th className="align-right">TOTAL</th><th>COBRO</th></tr></thead><tbody>{[...pedidos].reverse().map((pedido) => <tr key={pedido.id}><td><strong>{pedido.nombreComercial}</strong></td><td>{pedido.fechaConfirmacion ? new Date(pedido.fechaConfirmacion).toLocaleDateString('es-AR') : new Date(pedido.fechaCreacion).toLocaleDateString('es-AR')}</td><td>{pedido.lineas?.reduce((total, linea) => total + linea.cantidadPresentaciones, 0) || 0} presentaciones</td><td><span className={`status status-${String(pedido.estado).toLowerCase()}`}>{pedido.estado}</span></td><td className="align-right">{money(pedido.total)}</td><td><button type="button" className="button secondary" onClick={() => abrirCobro(pedido)}>Cobrar</button></td></tr>)}</tbody></table></div>}</section></>}
                    {screen === 'cobro' && cobroPedido && <>
                      <div className="page-heading"><div><p className="eyebrow">COBRO</p><h1>{cobroPedido.nombreComercial}</h1><p className="muted">Pedido {cobroPedido.estado} · {money(cobroPedido.total)}</p></div><button type="button" className="button secondary" onClick={() => setScreen('pedidos')}>Volver a pedidos</button></div>
                      <section className="panel">
                        <div className="panel-heading"><div><h2>Intentos de cobro</h2><p>El pedido se completa cuando administración acredita el pago total.</p></div><span className="count-badge">{pagosDelPedido.length} intentos</span></div>
                        {pagosDelPedido.length === 0 ? <div className="empty-state"><h3>Sin cobros iniciados</h3><p>Elegí cómo paga el cliente para generar el cobro.</p></div> : <div className="table-wrap"><table><thead><tr><th>MEDIO</th><th>ESTADO</th><th className="align-right">IMPORTE</th><th>DETALLE</th></tr></thead><tbody>{pagosDelPedido.map((pago) => <tr key={pago.id}><td>{medioLegible(pago.medio)}</td><td><span className={`status status-${String(pago.estado).toLowerCase()}`}>{pago.estado}</span></td><td className="align-right">{money(pago.importe)}</td><td>{pago.referenciaExterna || pago.instrucciones || '—'}</td></tr>)}</tbody></table></div>}
                        {pagosDelPedido.filter((pago) => pago.enlacePago).map((pago) => <p key={`enlace-${pago.id}`}><a href={pago.enlacePago} target="_blank" rel="noreferrer">Abrir el pago de Mercado Pago</a></p>)}
                        {cobroPedido.estado === 'CONFIRMADO' && <div className="summary-line"><span>Iniciar cobro</span><div><button type="button" className="button secondary" disabled={guardando} onClick={() => iniciarCobro('TRANSFERENCIA')}>Transferencia</button> <button type="button" className="button secondary" disabled={guardando} onClick={() => iniciarCobro('MERCADO_PAGO')}>Mercado Pago</button> <button type="button" className="button secondary" disabled={guardando} onClick={() => iniciarCobro('EFECTIVO')}>Efectivo</button></div></div>}
                        {pagosDelPedido.some((pago) => pago.estado === 'ACREDITADO') && <button type="button" className="button primary full" onClick={() => descargarComprobante(cobroPedido.id)}>Descargar comprobante</button>}
                      </section>
                    </>}
                    {screen === 'ruta' && <>
                      <div className="page-heading"><div><p className="eyebrow">PLAN DEL DÍA</p><h1>Ruta de visitas</h1><p className="muted">{plan.fecha || 'Hoy'} · orden sugerido por cercanía</p></div><div className="heading-actions"><button type="button" className="button secondary" onClick={cargarPlan} disabled={planCargando}>Actualizar</button><button type="button" className="button primary" onClick={generarPlan} disabled={planCargando}>Generar plan</button><button type="button" className="button secondary" onClick={() => setScreen('cartera')}>Volver</button></div></div>
                      <MapaPuntos puntos={plan.visitas} />
                      <section className="panel">
                        <div className="panel-heading"><div><h2>Paradas del día</h2><p>Usá las flechas para cambiar el recorrido.</p></div><span className="count-badge">{plan.visitas.length} visitas</span></div>
                        {plan.visitas.length === 0 ? <div className="empty-state"><h3>Sin visitas planificadas</h3><p>Generá el plan del día a partir de tu cartera.</p></div> : <div className="table-wrap"><table><thead><tr><th>#</th><th>COMERCIO</th><th>DIRECCIÓN</th><th></th></tr></thead><tbody>{plan.visitas.map((visita, indice) => <tr key={visita.id}><td>{visita.orden}</td><td><strong>{visita.nombreComercial}</strong></td><td>{visita.calle} {visita.numeracion}{visita.localidad ? ` · ${visita.localidad}` : ''}</td><td><div className="row-actions"><button type="button" className="button tiny" disabled={planCargando || indice === 0} onClick={() => moverVisita(indice, -1)}>↑</button><button type="button" className="button tiny" disabled={planCargando || indice === plan.visitas.length - 1} onClick={() => moverVisita(indice, 1)}>↓</button><button type="button" className="button tiny" onClick={() => { setPuntoId(visita.idPuntoVenta); setScreen('pedido') }}>Pedido</button></div></td></tr>)}</tbody></table></div>}
                        <p className="muted mt-4">Mapas y datos geográficos © colaboradores de <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">OpenStreetMap</a>.</p>
                      </section>
                    </>}
                  </section>
        <footer className="footer">Dulce Mayor <span>·</span> Herramienta de gestión comercial</footer>
      </main>
    </div>
  )
}

export default App
