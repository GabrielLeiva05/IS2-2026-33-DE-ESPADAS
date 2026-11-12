import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import { Alerta, Encabezado } from '../componentes/Interfaz.jsx'

/** Cambio de clave: obligatorio en el primer ingreso (HU-06) o voluntario desde el perfil. */
export default function CambiarClave() {
  const { usuario, cambiarClave, salir } = useAuth()
  const navegar = useNavigate()
  const [datos, setDatos] = useState({ claveActual: '', nuevaClave: '', confirmarClave: '' })
  const [error, setError] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (!usuario) return <Navigate to="/ingresar" replace />
  const obligatorio = usuario.debeCambiarClave

  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }))

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setError(null)
    try {
      await cambiarClave(datos.claveActual, datos.nuevaClave, datos.confirmarClave)
      navegar('/', { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="pantalla">
      <Encabezado titulo={obligatorio ? 'Elegí tu contraseña' : 'Cambiar contraseña'} volver={obligatorio ? null : '/perfil'} />
      <main className="contenido">
        {obligatorio && (
          <Alerta tipo="info">Es tu primer ingreso: reemplazá la contraseña provisoria por una propia para continuar.</Alerta>
        )}
        <form className="tarjeta formulario" onSubmit={enviar}>
          <Alerta>{error}</Alerta>
          <label className="campo"><span>{obligatorio ? 'Contraseña provisoria' : 'Contraseña actual'}</span>
            <input className="control" type="password" autoComplete="current-password" required value={datos.claveActual} onChange={cambiar('claveActual')} />
          </label>
          <label className="campo"><span>Nueva contraseña</span>
            <input className="control" type="password" autoComplete="new-password" minLength={8} maxLength={72} required value={datos.nuevaClave} onChange={cambiar('nuevaClave')} />
            <small className="ayuda">Entre 8 y 72 caracteres, con letras y números.</small>
          </label>
          <label className="campo"><span>Repetí la nueva contraseña</span>
            <input className="control" type="password" autoComplete="new-password" minLength={8} maxLength={72} required value={datos.confirmarClave} onChange={cambiar('confirmarClave')} />
          </label>
          <button className="boton boton-primario" type="submit" disabled={enviando}>{enviando ? 'Guardando…' : 'Guardar contraseña'}</button>
          {obligatorio && <button className="boton boton-fantasma" type="button" onClick={salir}>Salir</button>}
        </form>
      </main>
    </div>
  )
}
