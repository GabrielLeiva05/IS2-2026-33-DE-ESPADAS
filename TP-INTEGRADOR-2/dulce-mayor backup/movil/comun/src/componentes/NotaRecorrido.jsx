/**
 * Explica, sin exagerar, cómo se dibujó el trayecto (por calles con OSRM o en línea recta) y cómo se decidió
 * el orden de las paradas (criterioOrden lo informa el backend: óptimo, aproximado o el armado por administración).
 */
export default function NotaRecorrido({ trayecto, criterioOrden }) {
  if (!trayecto?.geometria || trayecto.geometria.length < 2) return null
  const tiempo = trayecto.minutos > 0 ? ` · ${trayecto.kilometros.toFixed(1)} km, unos ${Math.round(trayecto.minutos)} min` : ''
  return (
    <p className="mapa-nota">
      {trayecto.fuente}{tiempo}.{' '}
      {criterioOrden ?? 'Orden armado por administración: todavía no se optimizó desde tu ubicación'}.
    </p>
  )
}
