import axios from 'axios'
import { leerSesion } from './sesionGuardada.js'

// Cliente HTTP único de las apps móviles. Todas las llamadas a la API REST pasan por acá (Axios).
// La URL del backend se configura con la variable VITE_API_URL (archivo .env de cada app).
export const URL_API = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '')

export const api = axios.create({
  baseURL: URL_API,
  timeout: 20000,
  headers: { Accept: 'application/json' },
})

// Agrega el token JWT a cada solicitud.
api.interceptors.request.use((config) => {
  const sesion = leerSesion()
  if (sesion?.token) {
    config.headers.Authorization = `Bearer ${sesion.token}`
  }
  return config
})

// Avisos globales: sesión vencida (401) o cambio de clave obligatorio (403 + X-Accion-Requerida).
api.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const estado = error.response?.status
    const esLogin = error.config?.url?.includes('/api/auth/login')
    if (estado === 401 && !esLogin) {
      window.dispatchEvent(new CustomEvent('dm:sesion-vencida'))
    }
    if (estado === 403 && error.response?.headers?.['x-accion-requerida'] === 'CAMBIAR_CLAVE') {
      window.dispatchEvent(new CustomEvent('dm:cambiar-clave'))
    }
    return Promise.reject(error)
  },
)

/** Mensaje legible de un error de la API (formato ErrorDTO del backend: { estado, error, mensaje }). */
export function mensajeDeError(error, porDefecto = 'No se pudo completar la operación.') {
  if (!error?.response) {
    return 'No hay conexión con el servidor. Revisá la señal e intentá de nuevo.'
  }
  const mensaje = error.response.data?.mensaje
  return typeof mensaje === 'string' && mensaje.trim() ? mensaje : porDefecto
}

export function esNoEncontrado(error) {
  return error?.response?.status === 404
}

/** URL pública de una imagen de producto. */
export function urlImagen(idImagen) {
  return idImagen ? `${URL_API}/api/imagenes/${encodeURIComponent(idImagen)}/contenido` : null
}
