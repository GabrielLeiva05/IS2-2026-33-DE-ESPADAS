import { api } from '@comun/api/cliente.js'

// Endpoints REST que usa la app del preventista (todos exigen el rol PREVENTISTA en el backend,
// salvo el catálogo y las categorías, que son públicos).

const conUbicacion = (posicion) => (posicion ? { lat: posicion.latitud, lon: posicion.longitud } : {})

export const rutas = {
  /** Ruta de visitas de hoy, con el siguiente punto sugerido desde la posición (si se informa). */
  miRuta: (posicion) => api.get('/api/rutas/mia', { params: conUbicacion(posicion) }).then((r) => r.data),
  /** Optimiza en el backend el orden de los pendientes desde la posición actual (OSRM /table + optimizador). */
  ordenar: (posicion) => api.post('/api/rutas/mia/ordenar', { latitud: posicion.latitud, longitud: posicion.longitud }).then((r) => r.data),
  /** Visita sin pedido (botón "Siguiente punto" sin cargar pedido). */
  sinPedido: (idPuntoVenta, motivo) => api.post(`/api/rutas/mia/puntos/${encodeURIComponent(idPuntoVenta)}/sin-pedido`, { motivo }),
}

export const catalogo = {
  productos: (filtros) => api.get('/api/catalogo', { params: filtros }).then((r) => r.data),
  categorias: () => api.get('/api/categorias').then((r) => r.data.filter((c) => !c.eliminado)),
}

export const pedidos = {
  cargar: (idPuntoVenta, detalles) => api.post('/api/pedidos/preventista', { idPuntoVenta, detalles }).then((r) => r.data),
  mios: () => api.get('/api/pedidos/preventista').then((r) => r.data),
  uno: (id) => api.get(`/api/pedidos/preventista/${encodeURIComponent(id)}`).then((r) => r.data),
  /** Confirma el pedido en forma presencial con el medio de pago preferido por el cliente. */
  confirmar: (id, formaDePago) => api
    .post(`/api/pedidos/preventista/${encodeURIComponent(id)}/confirmar`, { formaDePago })
    .then((r) => r.data),
}

export const clientes = {
  buscarPorCorreo: (correo) => api.get('/api/clientes/buscar', { params: { correo } }).then((r) => r.data),
  altaEnCampo: (datos) => api.post('/api/clientes/campo', datos).then((r) => r.data),
}

export const puntosVenta = {
  uno: (id) => api.get(`/api/puntos-venta/${encodeURIComponent(id)}`).then((r) => r.data),
  delCliente: (idCliente) => api.get(`/api/puntos-venta/cliente/${encodeURIComponent(idCliente)}`).then((r) => r.data),
  alta: (datos) => api.post('/api/puntos-venta', datos).then((r) => r.data),
}

export const mapas = {
  geocodificar: (direccion) => api.get('/api/mapas/geocodificar', { params: { direccion } }).then((r) => r.data),
  inversa: (lat, lon) => api.get('/api/mapas/geocodificacion-inversa', { params: { lat, lon } }).then((r) => r.data),
}

export const ubicaciones = {
  provincia: (nombre) => api.get('/api/provincias/buscar', { params: { nombre } }).then((r) => r.data),
  departamentos: (idProvincia) => api.get(`/api/departamentos/provincia/${encodeURIComponent(idProvincia)}`).then((r) => r.data),
  localidades: (idDepartamento) => api.get(`/api/localidades/departamento/${encodeURIComponent(idDepartamento)}`).then((r) => r.data),
}
