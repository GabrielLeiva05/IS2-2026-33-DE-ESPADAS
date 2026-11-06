import { useEffect, useMemo } from 'react'
import L from 'leaflet'
import { MapContainer, Marker, Polyline, TileLayer, Tooltip, useMap, useMapEvents, CircleMarker } from 'react-leaflet'

// Componente de mapa compartido por la app del preventista y la del distribuidor (HU-19).
// Teselas: OpenStreetMap (uso sujeto a su política: tráfico moderado y atribución visible).
const TESELAS = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const ATRIBUCION = '&copy; <a href="https://www.openstreetmap.org/copyright">colaboradores de OpenStreetMap</a>'

const TONO_PIN = {
  PENDIENTE: 'pendiente', CON_PEDIDO: 'hecho', ENTREGADO: 'hecho', SIN_PEDIDO: 'aviso', NO_ENTREGADO: 'error',
}

function iconoNumerado(numero, resultado, destacado) {
  const clase = `dm-pin dm-pin-${TONO_PIN[resultado] ?? 'pendiente'}${destacado ? ' dm-pin-destacado' : ''}`
  return L.divIcon({
    className: 'dm-pin-contenedor',
    html: `<span class="${clase}"><span>${numero}</span></span>`,
    iconSize: [34, 42],
    iconAnchor: [17, 40],
    tooltipAnchor: [0, -36],
  })
}

/** Encuadra el mapa para que se vean todos los puntos (y la posición del usuario). */
function Encuadre({ coordenadas }) {
  const mapa = useMap()
  const clave = coordenadas.map((c) => c.join(',')).join(';')
  useEffect(() => {
    if (coordenadas.length === 1) mapa.setView(coordenadas[0], 15)
    else if (coordenadas.length > 1) mapa.fitBounds(coordenadas, { padding: [36, 36], maxZoom: 16 })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clave, mapa])
  return null
}

function ClicEnMapa({ alTocar }) {
  useMapEvents({ click: (e) => alTocar?.(e.latlng.lat, e.latlng.lng) })
  return null
}

/**
 * @param puntos     PuntoRutaDTO[] (orden, resultado, latitud, longitud, nombreFantasia)
 * @param posicion   { latitud, longitud } del usuario o null
 * @param trayecto   TrayectoDTO (geometria [[lat, lon], ...], fuente)
 * @param destacado  idPuntoVenta resaltado (siguiente sugerido)
 * @param alElegir   callback(idPuntoVenta) al tocar un pin
 * @param alTocarMapa callback(lat, lon) al tocar el mapa (para marcar el inicio a mano)
 */
export default function MapaRuta({ puntos = [], posicion, trayecto, destacado, alElegir, alTocarMapa, alto = 300 }) {
  const conUbicacion = useMemo(() => puntos.filter((p) => p.latitud != null && p.longitud != null), [puntos])
  const coordenadas = useMemo(() => {
    const lista = conUbicacion.map((p) => [p.latitud, p.longitud])
    if (posicion) lista.push([posicion.latitud, posicion.longitud])
    return lista
  }, [conUbicacion, posicion])
  const linea = trayecto?.geometria?.length > 1 ? trayecto.geometria : null

  if (coordenadas.length === 0 && !alTocarMapa) {
    return <div className="mapa mapa-vacio" style={{ height: alto }}>No hay puntos con ubicación para mostrar.</div>
  }

  return (
    <div className="mapa" style={{ height: alto }}>
      <MapContainer
        center={coordenadas[0] ?? [-32.89, -68.83]}
        zoom={coordenadas.length ? 14 : 11}
        scrollWheelZoom={false}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer url={TESELAS} attribution={ATRIBUCION} maxZoom={19} />
        <Encuadre coordenadas={coordenadas} />
        {alTocarMapa && <ClicEnMapa alTocar={alTocarMapa} />}
        {linea && (
          <Polyline
            positions={linea}
            pathOptions={{ color: '#7a35e0', weight: 5, opacity: 0.75, dashArray: trayecto.fuente?.startsWith('Línea recta') ? '8 8' : null }}
          />
        )}
        {posicion && (
          <CircleMarker center={[posicion.latitud, posicion.longitud]} radius={9}
            pathOptions={{ color: '#fff', weight: 3, fillColor: '#1f6feb', fillOpacity: 1 }}>
            <Tooltip direction="top">{posicion.manual ? 'Inicio marcado' : 'Estás acá'}</Tooltip>
          </CircleMarker>
        )}
        {conUbicacion.map((p) => (
          <Marker
            key={p.idPuntoVenta}
            position={[p.latitud, p.longitud]}
            icon={iconoNumerado(p.orden, p.resultado, p.idPuntoVenta === destacado)}
            eventHandlers={alElegir ? { click: () => alElegir(p.idPuntoVenta) } : undefined}
          >
            <Tooltip direction="top">{p.orden}. {p.nombreFantasia}</Tooltip>
          </Marker>
        ))}
      </MapContainer>
    </div>
  )
}
