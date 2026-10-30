import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { authApi, onSesionExpirada, perfilApi, tokenStorage } from '../api/api'

const AuthContext = createContext(null)

/**
 * Estado de la sesion de toda la app:
 *  - token: JWT guardado en localStorage (sobrevive a recargar la pagina)
 *  - usuario: datos de /api/perfil
 */
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(() => Boolean(tokenStorage.get()))
  // Mensaje de bienvenida que se muestra en el perfil despues de registrarse
  const [bienvenida, setBienvenida] = useState(false)

  const logout = useCallback(() => {
    tokenStorage.clear()
    setUsuario(null)
    setBienvenida(false)
  }, [])

  // Guarda la respuesta de login / registro / edicion de perfil: { token, usuario }
  const guardarSesion = useCallback(({ token, usuario }, { nuevo = false } = {}) => {
    tokenStorage.set(token)
    setUsuario(usuario)
    if (nuevo) setBienvenida(true)
  }, [])

  const login = useCallback(async (correo, clave) => {
    const respuesta = await authApi.login(correo, clave)
    guardarSesion(respuesta)
    return respuesta.usuario
  }, [guardarSesion])

  // Login con Google: el backend devuelve solo el token en la URL; los datos se piden a /api/perfil
  const loginConToken = useCallback(async (token) => {
    tokenStorage.set(token)
    const datos = await perfilApi.obtener()
    setUsuario(datos)
    return datos
  }, [])

  // Al abrir la app, si habia un token guardado se recupera el usuario
  useEffect(() => {
    onSesionExpirada(logout)
    if (!tokenStorage.get()) return
    perfilApi.obtener()
      .then(setUsuario)
      .catch(() => logout())
      .finally(() => setCargando(false))
  }, [logout])

  const valor = {
    usuario, setUsuario, cargando, login, loginConToken, guardarSesion, logout,
    bienvenida, ocultarBienvenida: () => setBienvenida(false),
  }
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext)
}
