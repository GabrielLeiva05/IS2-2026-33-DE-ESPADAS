import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Navbar from './Navbar'

/** Solo deja pasar si hay sesion (y, opcionalmente, si el usuario tiene el rol pedido). */
export default function RutaProtegida({ rol }) {
  const { usuario, cargando } = useAuth()

  if (cargando) return <Cargando />
  if (!usuario) return <Navigate to="/login" replace />
  if (rol && usuario.rol !== rol) return <Navigate to="/perfil" replace />

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="max-w-5xl mx-auto px-4 sm:px-6 py-10">
        <Outlet />
      </main>
    </div>
  )
}

/** Login y registro: si ya hay sesion, van directo al perfil. */
export function RutaPublica() {
  const { usuario, cargando } = useAuth()
  if (cargando) return <Cargando />
  return usuario ? <Navigate to="/perfil" replace /> : <Outlet />
}

export function Cargando() {
  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center">
      <div className="h-10 w-10 rounded-full border-4 border-indigo-200 border-t-indigo-600 animate-spin" />
    </div>
  )
}
