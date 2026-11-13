import L from 'leaflet'
import { useEffect } from 'react'
import { MapContainer, Marker, TileLayer, useMap, useMapEvents } from 'react-leaflet'

const ICONO = L.divIcon({
  className: 'dm-pin-contenedor',
  html: '<span class="dm-pin dm-pin-destacado"><span>●</span></span>',
  iconSize: [34, 42],
  iconAnchor: [17, 40],
})

function Seguir({ posicion }) {
  const mapa = useMap()
  useEffect(() => {
    if (posicion) mapa.setView([posicion.latitud, posicion.longitud], Math.max(mapa.getZoom(), 17))
  }, [posicion, mapa])
  return null
}

function Clic({ alTocar }) {
  useMapEvents({ click: (e) => alTocar(e.latlng.lat, e.latlng.lng) })
  return null
}

/**
 * Mapa para ubicar un comercio: se toca el lugar exacto o se mueve el pin.
 * Sin posición elegida, el mapa muestra la zona de Mendoza solo como vista inicial:
 * esa vista no se guarda como ubicación.
 */
export default function SelectorUbicacion({ posicion, alCambiar, alto = 240 }) {
  return (
    <div className="mapa" style={{ height: alto }}>
      <MapContainer center={[-32.89, -68.83]} zoom={12} scrollWheelZoom={false} style={{ height: '100%', width: '100%' }}>
        <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" maxZoom={19}
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">colaboradores de OpenStreetMap</a>' />
        <Seguir posicion={posicion} />
        <Clic alTocar={alCambiar} />
        {posicion && (
          <Marker position={[posicion.latitud, posicion.longitud]} icon={ICONO} draggable
            eventHandlers={{ dragend: (e) => { const p = e.target.getLatLng(); alCambiar(p.lat, p.lng) } }} />
        )}
      </MapContainer>
    </div>
  )
}
