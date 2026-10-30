import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const claseLink = ({ isActive }) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition ${
    isActive ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
  }`

export default function Navbar() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()

  const salir = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <header className="bg-white border-b border-slate-100 shadow-sm">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
        <Link to="/perfil" className="text-xl font-extrabold text-indigo-700 tracking-tight">Blockazo</Link>
        <nav className="flex items-center gap-1">
          <NavLink to="/perfil" className={claseLink}>Mi perfil</NavLink>
          {usuario?.rol === 'ADMIN' && <NavLink to="/admin/usuarios" className={claseLink}>Usuarios</NavLink>}
          <span className="hidden sm:inline text-sm text-slate-400 px-3 border-l border-slate-200 ml-2">
            {usuario?.correo}
          </span>
          <button onClick={salir} className="px-3 py-2 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition cursor-pointer">
            Salir
          </button>
        </nav>
      </div>
    </header>
  )
}
