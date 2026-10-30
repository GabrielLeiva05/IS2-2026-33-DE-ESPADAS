import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import AdminUsuarios from './components/AdminUsuarios'
import Login from './components/Login'
import OAuthCallback from './components/OAuthCallback'
import Perfil from './components/Perfil'
import Registro from './components/Registro'
import RutaProtegida, { RutaPublica } from './components/RutaProtegida'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Publicas: si ya hay sesion redirigen al perfil */}
          <Route element={<RutaPublica />}>
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registro />} />
          </Route>

          {/* Vuelta del login con Google (?token=... o ?error=...) */}
          <Route path="/auth/callback" element={<OAuthCallback />} />

          {/* Protegidas: requieren el JWT */}
          <Route element={<RutaProtegida />}>
            <Route path="/perfil" element={<Perfil />} />
          </Route>
          <Route element={<RutaProtegida rol="ADMIN" />}>
            <Route path="/admin/usuarios" element={<AdminUsuarios />} />
          </Route>

          {/* Cualquier otra ruta */}
          <Route path="*" element={<Navigate to="/perfil" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
