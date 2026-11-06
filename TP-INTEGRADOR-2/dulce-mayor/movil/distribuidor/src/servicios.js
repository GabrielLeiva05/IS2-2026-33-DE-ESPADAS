import { api } from '@comun/api/cliente.js'

// Endpoints REST de la app del distribuidor (el backend exige el rol DISTRIBUIDOR y usa el usuario del token:
// solo se ven y se modifican los pedidos asignados a quien inició sesión).
export const entregas = {
  /** Arma (o completa) la ruta de entregas de hoy con los comercios de los pedidos asignados y la ordena por cercanía. */
  generarRuta: (posicion) => api.post('/api/pedidos/entregas/ruta',
    posicion ? { latitud: posicion.latitud, longitud: posicion.longitud } : null).then((r) => r.data),
  consultarRuta: (posicion) => api.get('/api/pedidos/entregas/ruta',
    { params: posicion ? { lat: posicion.latitud, lon: posicion.longitud } : {} }).then((r) => r.data),
  pendientes: () => api.get('/api/pedidos/entregas/pendientes').then((r) => r.data),
  entregado: (idPuntoVenta) => api.post(`/api/pedidos/entregas/${encodeURIComponent(idPuntoVenta)}/entregado`).then((r) => r.data),
  noEntregado: (idPuntoVenta, motivo) => api.post(`/api/pedidos/entregas/${encodeURIComponent(idPuntoVenta)}/no-entregado`, { motivo }),
}
