const moneda = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 2 })

export const formatoMoneda = (valor) => moneda.format(Number(valor) || 0)

/** "2026-11-03T10:15:30" → "03/11 10:15" */
export function formatoFechaHora(texto) {
  if (!texto) return ''
  const d = new Date(texto)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleString('es-AR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

export const formatoKm = (km) => (km == null ? '' : km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1).replace('.', ',')} km`)

export const RESULTADOS = {
  PENDIENTE: { texto: 'Pendiente', tono: 'gris' },
  CON_PEDIDO: { texto: 'Con pedido', tono: 'verde' },
  SIN_PEDIDO: { texto: 'Sin pedido', tono: 'naranja' },
  ENTREGADO: { texto: 'Entregado', tono: 'verde' },
  NO_ENTREGADO: { texto: 'No entregado', tono: 'rojo' },
}

export const ESTADOS_PEDIDO = {
  CARGADO: { texto: 'Cargado · espera confirmación', tono: 'naranja' },
  PENDIENTE_DE_ENTREGA: { texto: 'Pendiente de entrega', tono: 'azul' },
  ENTREGADO: { texto: 'Entregado', tono: 'verde' },
  ANULADO: { texto: 'Anulado', tono: 'gris' },
}

export function presentacionLegible(presentacion, unidades) {
  if (!presentacion) return ''
  const texto = presentacion.charAt(0) + presentacion.slice(1).toLowerCase().replace('_', ' ')
  return unidades > 1 ? `${texto} x ${unidades}` : texto
}
