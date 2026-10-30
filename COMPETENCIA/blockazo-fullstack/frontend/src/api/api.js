// Cliente de la API REST del backend (Spring Boot).
// - Agrega el JWT en el header Authorization de cada peticion.
// - Convierte las respuestas de error del backend ({ status, mensaje, errores }) en ApiError.

// En desarrollo queda vacio: las llamadas van a /api/... y el proxy de Vite las manda al 8080.
// Si se quisiera apuntar a otro servidor: crear .env con VITE_API_URL=http://servidor:8080
const BASE_URL = import.meta.env.VITE_API_URL ?? ''

const TOKEN_KEY = 'blockazo_token'

export const tokenStorage = {
  get() {
    try { return localStorage.getItem(TOKEN_KEY) } catch { return null }
  },
  set(token) {
    try { localStorage.setItem(TOKEN_KEY, token) } catch { /* sin storage: solo dura la pestaña */ }
  },
  clear() {
    try { localStorage.removeItem(TOKEN_KEY) } catch { /* nada */ }
  },
}

export class ApiError extends Error {
  constructor(status, mensaje, errores = null) {
    super(mensaje)
    this.status = status
    this.errores = errores // errores por campo: { dni: 'El DNI debe tener...' }
  }
}

// AuthContext registra aca que hacer cuando el token vence (cerrar sesion)
let alExpirarSesion = null
export function onSesionExpirada(fn) {
  alExpirarSesion = fn
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  const token = auth ? tokenStorage.get() : null
  if (token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${BASE_URL}/api${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verificá que el backend esté levantado en el puerto 8080.')
  }

  if (res.status === 204) return null

  const texto = await res.text()
  let data
  try { data = texto ? JSON.parse(texto) : null } catch { data = null }

  if (!res.ok) {
    if (res.status === 401 && token && alExpirarSesion) alExpirarSesion()
    throw new ApiError(res.status, data?.mensaje || `Error ${res.status}`, data?.errores || null)
  }
  return data
}

// URL a la que se navega (navegacion completa, no fetch) para ingresar con Google
export const GOOGLE_LOGIN_URL = `${BASE_URL}/oauth2/authorization/google`

// ---------------- Endpoints ----------------

export const authApi = {
  // -> { token, tipo, usuario }
  login: (correo, clave) => request('/auth/login', { method: 'POST', body: { correo, clave }, auth: false }),
  // datos: { nombre, apellido, dni, fechaNacimiento, direccion, correo, clave, confirmacionClave }
  registrar: (datos) => request('/auth/register', { method: 'POST', body: datos, auth: false }),
}

export const perfilApi = {
  obtener: () => request('/perfil'),
  // -> { token, tipo, usuario } (token nuevo por si cambio el correo)
  actualizar: (datos) => request('/perfil', { method: 'PUT', body: datos }),
  cambiarClave: (datos) => request('/perfil/clave', { method: 'PUT', body: datos }),
}

// ABM de usuarios (solo ADMIN) - patron Template en el backend
export const usuariosApi = {
  listar: () => request('/usuarios'),
  obtener: (id) => request(`/usuarios/${id}`),
  crear: (usuario) => request('/usuarios', { method: 'POST', body: usuario }),
  modificar: (id, usuario) => request(`/usuarios/${id}`, { method: 'PUT', body: usuario }),
  eliminar: (id) => request(`/usuarios/${id}`, { method: 'DELETE' }),
}
