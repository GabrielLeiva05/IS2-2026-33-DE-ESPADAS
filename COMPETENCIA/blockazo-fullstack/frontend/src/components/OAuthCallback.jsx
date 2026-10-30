import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Cargando } from './RutaProtegida'

/**
 * Vuelta del login con Google. El backend redirige a:
 *   /auth/callback?token=<jwt>     -> se guarda el token y se va al perfil
 *   /auth/callback?error=<mensaje> -> se vuelve al login mostrando el error
 */
export default function OAuthCallback() {
  const [params] = useSearchParams()
  const { loginConToken } = useAuth()
  const navigate = useNavigate()
  const procesado = useRef(false) // StrictMode ejecuta los efectos dos veces en desarrollo

  useEffect(() => {
    if (procesado.current) return
    procesado.current = true

    const token = params.get('token')
    const error = params.get('error')

    if (!token) {
      navigate('/login', { replace: true, state: { error: error || 'No se pudo iniciar sesión con Google' } })
      return
    }
    loginConToken(token)
      .then(() => navigate('/perfil', { replace: true }))
      .catch((err) => navigate('/login', { replace: true, state: { error: err.message } }))
  }, [params, loginConToken, navigate])

  return <Cargando />
}
