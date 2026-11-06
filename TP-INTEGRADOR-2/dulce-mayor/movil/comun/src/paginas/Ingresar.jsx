import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import { Alerta } from '../componentes/Interfaz.jsx'
import Logo from '../componentes/Logo.jsx'

export default function Ingresar({ subtitulo }) {
  const { usuario, ingresar, aviso } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [verClave, setVerClave] = useState(false)
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) return <Navigate to={usuario.debeCambiarClave ? '/cambiar-clave' : '/'} replace />

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      const sesion = await ingresar(correo.trim(), clave)
      navegar(sesion.usuario.debeCambiarClave ? '/cambiar-clave' : (ubicacion.state?.desde ?? '/'), { replace: true })
    } catch (err) {
      setError(err.message)
      setClave('')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="pantalla-acceso">
      <div className="acceso-marca">
        <Logo />
        <p>{subtitulo}</p>
      </div>
      <form className="tarjeta acceso-formulario" onSubmit={enviar}>
        <h1>Ingresar</h1>
        <Alerta tipo="info">{aviso}</Alerta>
        <Alerta>{error}</Alerta>
        <label className="campo">
          <span>Correo electrónico</span>
          <input className="control" type="email" autoComplete="username" inputMode="email" required
            value={correo} onChange={(e) => setCorreo(e.target.value)} placeholder="nombre@dulcemayor.com" />
        </label>
        <label className="campo">
          <span>Contraseña</span>
          <span className="control-con-accion">
            <input className="control" type={verClave ? 'text' : 'password'} autoComplete="current-password" required
              value={clave} onChange={(e) => setClave(e.target.value)} />
            <button type="button" className="boton-texto" onClick={() => setVerClave((v) => !v)}>
              {verClave ? 'Ocultar' : 'Ver'}
            </button>
          </span>
        </label>
        <button className="boton boton-primario" type="submit" disabled={enviando}>
          {enviando ? 'Ingresando…' : 'Ingresar'}
        </button>
        <p className="ayuda">¿Primera vez? Usá la contraseña provisoria que te dio administración: te vamos a pedir que la cambies.</p>
      </form>
    </main>
  )
}
