import { Navigate, Outlet, Route, Routes } from 'react-router'
import RequireAuth from '@comun/auth/RequireAuth.jsx'
import { BarraInferior } from '@comun/componentes/Interfaz.jsx'
import Ingresar from '@comun/paginas/Ingresar.jsx'
import CambiarClave from '@comun/paginas/CambiarClave.jsx'
import Perfil from '@comun/paginas/Perfil.jsx'
import RutaEntregas from './paginas/RutaEntregas.jsx'
import DetalleEntrega from './paginas/DetalleEntrega.jsx'
import Pendientes from './paginas/Pendientes.jsx'

const SECCIONES = [
  { a: '/', texto: 'Mi ruta', icono: 'ruta', fin: true },
  { a: '/pedidos', texto: 'Pedidos', icono: 'camion' },
  { a: '/perfil', texto: 'Perfil', icono: 'usuario' },
]

function Interna() {
  return (
    <RequireAuth>
      <div className="pantalla-con-barra"><Outlet /></div>
      <BarraInferior items={SECCIONES} />
    </RequireAuth>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/ingresar" element={<Ingresar subtitulo="App del distribuidor" />} />
      <Route path="/cambiar-clave" element={<CambiarClave />} />
      <Route element={<Interna />}>
        <Route index element={<RutaEntregas />} />
        <Route path="entregas/:idPuntoVenta" element={<DetalleEntrega />} />
        <Route path="pedidos" element={<Pendientes />} />
        <Route path="perfil" element={<Perfil />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
