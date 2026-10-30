import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Alerta from './Alerta'
import BotonGoogle from './BotonGoogle'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  // Puede venir un error desde el login con Google (/auth/callback?error=...)
  const [errorBackend, setErrorBackend] = useState(location.state?.error || '')
  const [loading, setLoading] = useState(false)

  const handleLogin = async (e) => {
    e.preventDefault()
    setErrorBackend('')
    setLoading(true)
    try {
      await login(correo, clave)
      navigate('/perfil', { replace: true })
    } catch (err) {
      // Mensaje que envia el backend (ej: "Correo o clave incorrectos", "El usuario se encuentra dado de baja")
      setErrorBackend(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-slate-100">
        <div className="text-center">
          <p className="text-sm font-bold text-indigo-600 tracking-widest uppercase">Blockazo</p>
          <h2 className="mt-2 text-3xl font-extrabold text-slate-900">Iniciar Sesión</h2>
        </div>

        <Alerta tipo="error">{errorBackend}</Alerta>

        <form className="space-y-5" onSubmit={handleLogin}>
          <input
            type="email" required autoComplete="username" placeholder="Correo Electrónico"
            value={correo} onChange={(e) => setCorreo(e.target.value)} className="input"
          />
          <input
            type="password" required autoComplete="current-password" placeholder="Contraseña"
            value={clave} onChange={(e) => setClave(e.target.value)} className="input"
          />
          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Verificando...' : 'Ingresar'}
          </button>
        </form>

        <div className="flex items-center gap-3 text-xs text-slate-400 uppercase">
          <span className="h-px flex-1 bg-slate-200" /> o <span className="h-px flex-1 bg-slate-200" />
        </div>

        <BotonGoogle />

        <p className="text-center text-sm text-slate-500">
          ¿No tenés cuenta?{' '}
          <Link to="/registro" className="font-medium text-indigo-600 hover:text-indigo-700">Registrate</Link>
        </p>
      </div>
    </div>
  )
}
