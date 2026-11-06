import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import 'leaflet/dist/leaflet.css'
import '@comun/estilos.css'
import { AuthProvider } from '@comun/auth/AuthContext.jsx'
import { CarritoProvider } from './contexto/CarritoContext.jsx'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider rolesPermitidos={['PREVENTISTA']} nombreApp="preventistas">
        <CarritoProvider>
          <App />
        </CarritoProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
