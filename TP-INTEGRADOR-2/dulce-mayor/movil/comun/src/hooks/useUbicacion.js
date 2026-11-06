import { useCallback, useEffect, useState } from 'react'

/**
 * Ubicación actual del dispositivo (API de geolocalización del navegador, requiere HTTPS o localhost).
 * Si el usuario no da permiso o el GPS falla, `error` lo explica y la pantalla ofrece marcar el inicio
 * tocando el mapa (`elegirManual`). Nunca se inventa una coordenada por defecto.
 */
const HAY_GPS = typeof navigator !== 'undefined' && 'geolocation' in navigator

export default function useUbicacion({ seguir = true } = {}) {
  const [posicion, setPosicion] = useState(null)
  const [error, setError] = useState(HAY_GPS ? null
    : 'Este dispositivo no informa su ubicación. Tocá el mapa para marcar desde dónde salís.')
  const [manual, setManual] = useState(false)
  const [buscando, setBuscando] = useState(HAY_GPS)

  useEffect(() => {
    if (!HAY_GPS || manual) return undefined
    const exito = (p) => {
      setPosicion({ latitud: p.coords.latitude, longitud: p.coords.longitude, precision: p.coords.accuracy })
      setError(null)
      setBuscando(false)
    }
    const fallo = (e) => {
      setBuscando(false)
      setError(e.code === e.PERMISSION_DENIED
        ? 'No diste permiso para usar tu ubicación. Tocá el mapa para marcar desde dónde salís.'
        : 'No pudimos obtener tu ubicación. Tocá el mapa para marcar desde dónde salís.')
    }
    const opciones = { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 }
    if (!seguir) {
      navigator.geolocation.getCurrentPosition(exito, fallo, opciones)
      return undefined
    }
    const id = navigator.geolocation.watchPosition(exito, fallo, opciones)
    return () => navigator.geolocation.clearWatch(id)
  }, [seguir, manual])

  /** El usuario marca su posición en el mapa (deja de seguirse el GPS). */
  const elegirManual = useCallback((latitud, longitud) => {
    setManual(true)
    setPosicion({ latitud, longitud, precision: null, manual: true })
    setError(null)
    setBuscando(false)
  }, [])

  const volverAlGps = useCallback(() => {
    setManual(false)
    setBuscando(true)
  }, [])

  return { posicion, error, buscando, manual, elegirManual, volverAlGps }
}
