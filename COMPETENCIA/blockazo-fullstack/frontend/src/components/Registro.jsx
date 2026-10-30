import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/api'
import { useAuth } from '../context/AuthContext'
import Alerta from './Alerta'
import BotonGoogle from './BotonGoogle'
import Campo from './Campo'

const VACIO = {
  nombre: '', apellido: '', dni: '', fechaNacimiento: '', direccion: '',
  correo: '', clave: '', confirmacionClave: '',
}

export default function Registro() {
  const { guardarSesion } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState(VACIO)
  const [error, setError] = useState('')
  const [errores, setErrores] = useState({})
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: undefined })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setErrores({})

    if (form.clave !== form.confirmacionClave) {
      setErrores({ confirmacionClave: 'Las claves no coinciden' })
      return
    }

    setLoading(true)
    try {
      // El backend da de alta a la persona y devuelve el token: queda logueada directamente
      const respuesta = await authApi.registrar(form)
      guardarSesion(respuesta, { nuevo: true })
      navigate('/perfil', { replace: true })
    } catch (err) {
      setError(err.message)
      setErrores(err.errores || {})
    } finally {
      setLoading(false)
    }
  }

  const campo = (name, props) => (
    <Campo name={name} value={form[name]} onChange={handleChange} error={errores[name]} required {...props} />
  )

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-xl border border-slate-100">
        <div className="text-center">
          <p className="text-sm font-bold text-indigo-600 tracking-widest uppercase">Blockazo</p>
          <h2 className="mt-2 text-3xl font-extrabold text-slate-900">Crea tu cuenta</h2>
        </div>

        <Alerta tipo="error">{error}</Alerta>

        <form className="space-y-4" onSubmit={handleSubmit} noValidate>
          <div className="grid grid-cols-2 gap-4">
            {campo('nombre', { placeholder: 'Nombre', autoComplete: 'given-name' })}
            {campo('apellido', { placeholder: 'Apellido', autoComplete: 'family-name' })}
          </div>
          {campo('dni', { placeholder: 'DNI (sin puntos)', inputMode: 'numeric', maxLength: 8 })}
          {campo('fechaNacimiento', { type: 'date', label: 'Fecha de nacimiento' })}
          {campo('direccion', { placeholder: 'Dirección', autoComplete: 'street-address' })}
          {campo('correo', { type: 'email', placeholder: 'Correo Electrónico (será tu usuario)', autoComplete: 'email' })}
          {campo('clave', { type: 'password', placeholder: 'Contraseña (mínimo 6 caracteres)', autoComplete: 'new-password' })}
          {campo('confirmacionClave', { type: 'password', placeholder: 'Confirmar Contraseña', autoComplete: 'new-password' })}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? 'Procesando...' : 'Registrarse'}
          </button>
        </form>

        <BotonGoogle texto="Registrarse con Google" />

        <p className="text-center text-sm text-slate-500">
          ¿Ya tenés cuenta?{' '}
          <Link to="/login" className="font-medium text-indigo-600 hover:text-indigo-700">Iniciá sesión</Link>
        </p>
      </div>
    </div>
  )
}
