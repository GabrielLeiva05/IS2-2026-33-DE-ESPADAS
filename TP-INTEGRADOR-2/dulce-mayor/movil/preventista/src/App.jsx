import { Navigate, Outlet, Route, Routes } from 'react-router'
import RequireAuth from '@comun/auth/RequireAuth.jsx'
import { BarraInferior } from '@comun/componentes/Interfaz.jsx'
import Ingresar from '@comun/paginas/Ingresar.jsx'
import CambiarClave from '@comun/paginas/CambiarClave.jsx'
import Perfil from '@comun/paginas/Perfil.jsx'
import RutaDelDia from './paginas/RutaDelDia.jsx'
import DetallePunto from './paginas/DetallePunto.jsx'
import TomarPedido from './paginas/TomarPedido.jsx'
import ResumenPedido from './paginas/ResumenPedido.jsx'
import PedidoCargado from './paginas/PedidoCargado.jsx'
import MisPedidos from './paginas/MisPedidos.jsx'
import DetallePedido from './paginas/DetallePedido.jsx'
import NuevoCliente from './paginas/NuevoCliente.jsx'
import NuevoPuntoVenta from './paginas/NuevoPuntoVenta.jsx'
import Catalogo from './paginas/Catalogo.jsx'

const SECCIONES = [
  { a: '/', texto: 'Mi ruta', icono: 'ruta', fin: true },
  { a: '/catalogo', texto: 'Catálogo', icono: 'catalogo' },
  { a: '/pedidos', texto: 'Pedidos', icono: 'pedido' },
  { a: '/perfil', texto: 'Perfil', icono: 'usuario' },
]

/** Pantallas internas: requieren sesión de preventista y muestran la barra inferior. */
function Interna() {
  return (
    <RequireAuth>
      <div className="pantalla-con-barra">
        <Outlet />
      </div>
      <BarraInferior items={SECCIONES} />
    </RequireAuth>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/ingresar" element={<Ingresar subtitulo="App del preventista" />} />
      <Route path="/cambiar-clave" element={<CambiarClave />} />
      <Route element={<Interna />}>
        <Route index element={<RutaDelDia />} />
        <Route path="puntos/nuevo" element={<NuevoPuntoVenta />} />
        <Route path="puntos/:idPuntoVenta" element={<DetallePunto />} />
        <Route path="puntos/:idPuntoVenta/pedido" element={<TomarPedido />} />
        <Route path="puntos/:idPuntoVenta/resumen" element={<ResumenPedido />} />
        <Route path="pedidos" element={<MisPedidos />} />
        <Route path="pedidos/:id" element={<DetallePedido />} />
        <Route path="pedidos/:id/cargado" element={<PedidoCargado />} />
        <Route path="clientes/nuevo" element={<NuevoCliente />} />
        <Route path="catalogo" element={<Catalogo />} />
        <Route path="perfil" element={<Perfil />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
