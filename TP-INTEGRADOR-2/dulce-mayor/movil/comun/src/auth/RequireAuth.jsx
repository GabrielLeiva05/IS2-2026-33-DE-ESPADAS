import { Navigate, useLocation } from 'react-router'
import { useAuth } from './AuthContext.jsx'

/** Protege las pantallas internas: sin sesión → ingreso; con clave provisoria → cambio de clave. */
export default function RequireAuth({ children }) {
  const { usuario } = useAuth()
  const ubicacion = useLocation()
  if (!usuario) {
    return <Navigate to="/ingresar" replace state={{ desde: ubicacion.pathname }} />
  }
  if (usuario.debeCambiarClave) {
    return <Navigate to="/cambiar-clave" replace />
  }
  return children
}
