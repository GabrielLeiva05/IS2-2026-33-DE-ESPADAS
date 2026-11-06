import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, mensajeDeError } from '../api/cliente.js'
import { borrarSesion, guardarSesion, leerSesion } from '../api/sesionGuardada.js'

const AuthContext = createContext(null)

/**
 * Sesión del empleado en la app. `rolesPermitidos` limita quién puede usar cada app
 * (la API vuelve a controlar el rol en cada endpoint: esto solo evita entrar a una app ajena).
 */
export function AuthProvider({ rolesPermitidos, nombreApp, children }) {
  const [sesion, setSesion] = useState(() => {
    const guardada = leerSesion()
    return guardada && rolesPermitidos.includes(guardada.usuario?.rol) ? guardada : null
  })
  const [aviso, setAviso] = useState(null)

  const aplicar = useCallback((nueva) => {
    setSesion(nueva)
    if (nueva) guardarSesion(nueva)
    else borrarSesion()
  }, [])

  const ingresar = useCallback(async (correo, clave) => {
    try {
      const { data } = await api.post('/api/auth/login', { correo, clave })
      if (!rolesPermitidos.includes(data.usuario?.rol)) {
        throw new Error(`Esta aplicación es solo para ${nombreApp}.`)
      }
      setAviso(null)
      aplicar(data)
      return data
    } catch (error) {
      if (error.response?.status === 401) throw new Error('Correo o contraseña incorrectos.', { cause: error })
      throw new Error(error.response ? mensajeDeError(error) : error.message, { cause: error })
    }
  }, [aplicar, nombreApp, rolesPermitidos])

  const cambiarClave = useCallback(async (claveActual, nuevaClave, confirmarClave) => {
    try {
      const { data } = await api.put('/api/auth/clave', { claveActual, nuevaClave, confirmarClave })
      aplicar(data)
      return data
    } catch (error) {
      throw new Error(mensajeDeError(error), { cause: error })
    }
  }, [aplicar])

  const salir = useCallback(() => aplicar(null), [aplicar])

  // Eventos que emite el cliente Axios ante 401 o cambio de clave obligatorio.
  useEffect(() => {
    const vencida = () => {
      aplicar(null)
      setAviso('Tu sesión venció. Volvé a ingresar.')
    }
    const exigirCambio = () => {
      setSesion((actual) => {
        if (!actual || actual.usuario.debeCambiarClave) return actual
        const nueva = { ...actual, usuario: { ...actual.usuario, debeCambiarClave: true } }
        guardarSesion(nueva)
        return nueva
      })
    }
    window.addEventListener('dm:sesion-vencida', vencida)
    window.addEventListener('dm:cambiar-clave', exigirCambio)
    return () => {
      window.removeEventListener('dm:sesion-vencida', vencida)
      window.removeEventListener('dm:cambiar-clave', exigirCambio)
    }
  }, [aplicar])

  const valor = useMemo(() => ({
    sesion, usuario: sesion?.usuario ?? null, aviso, nombreApp, ingresar, cambiarClave, salir,
  }), [sesion, aviso, nombreApp, ingresar, cambiarClave, salir])

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  return contexto
}
